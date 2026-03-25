package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public KeycloakJwtAuthenticationConverter(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
                                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        // 1. Leer roles del claim "roles" (plano)
        List<String> roles = jwt.getClaimAsStringList("roles");

        // Fallback: Si no hay claim "roles", buscar en "realm_access" (estándar de Keycloak)
        if (roles == null || roles.isEmpty()) {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                roles = realmRoles;
            }
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            log.debug("[Gateway] Roles extraídos: {}", roles);
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));
        } else {
            log.warn("[Gateway] No se encontraron roles en el token para el usuario: {}", jwt.getSubject());
        }

        // 2. Extraer user_id y municipal_code
        String userId = jwt.getClaimAsString("user_id");
        String municipalCode = jwt.getClaimAsString("municipal_code");

        if (userId == null) {
            return Mono.just((AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities));
        }

        String redisKey = "perms:" + userId + ":" + (municipalCode != null ? municipalCode : "platform");

        // 3. Enriquecer con permisos granulares desde Redis
        return redisTemplate.opsForValue()
                .get(redisKey)
                .map(this::parseJson)
                .defaultIfEmpty(Collections.emptyList())
                .map(permissions -> {
                    List<GrantedAuthority> granular = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    authorities.addAll(granular);
                    return (AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities);
                })
                .doOnError(e -> log.error("[Gateway] Error al obtener permisos desde Redis: {}", e.getMessage()))
                .onErrorReturn((AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities));
    }

    private List<String> parseJson(String json) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("[Gateway] Error al parsear permisos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
