package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Value("${cors.allowed-origins:*}")
        private String allowedOrigins;

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                        KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .cors(Customizer.withDefaults())
                                .authorizeExchange(exchanges -> exchanges
                                                // ENDPOINTS PÚBLICOS
                                                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                                                .pathMatchers("/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                                                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                                                "/favicon.ico")
                                                .permitAll()
                                                .pathMatchers("/actuator/**").permitAll()

                                                // MICROSERVICIO 01: TENANT MANAGEMENT
                                                .pathMatchers(HttpMethod.GET, "/api/v1/municipalities/**")
                                                .hasAuthority("municipality:read")
                                                .pathMatchers("/api/v1/municipalities/**")
                                                .hasAuthority("municipality:manage")

                                                // MICROSERVICIO 02: AUTHENTICATION (ASIGNACIONES, ROLES, PERMISOS, USUARIOS)
                                                .pathMatchers(HttpMethod.POST, "/api/v1/auth/logout",
                                                                "/api/v1/auth/validate")
                                                .authenticated()
                                                .pathMatchers(HttpMethod.GET,
                                                                "/api/v1/assignments/users/*/effective-permissions")
                                                .authenticated()
                                                .pathMatchers(HttpMethod.GET, "/api/v1/permissions/user/**")
                                                .authenticated()

                                                .pathMatchers("/api/v1/assignments/users/*/roles/**")
                                                .hasRole("TENANT_ADMIN")
                                                .pathMatchers("/api/v1/assignments/roles/*/permissions/**")
                                                .hasRole("TENANT_ADMIN")
                                                .pathMatchers("/api/v1/permissions/**").hasRole("TENANT_ADMIN")
                                                .pathMatchers("/api/v1/roles/**").hasRole("TENANT_ADMIN")

                                                .pathMatchers(HttpMethod.GET, "/api/v1/persons")
                                                .hasAnyRole("TENANT_ADMIN", "TENANT_CONFIG_MANAGER")
                                                .pathMatchers("/api/v1/persons/**").hasRole("TENANT_ADMIN")

                                                .pathMatchers("/api/v1/users/sync", "/api/v1/users/onboarding")
                                                .hasAnyRole("SUPER_ADMIN", "ONBOARDING_MANAGER")
                                                .pathMatchers("/api/v1/users/**").hasRole("TENANT_ADMIN")

                                                // MICROSERVICIO 03: CONFIGURATION
                                                .pathMatchers(HttpMethod.GET, "/api/v1/config/**",
                                                                "/api/v1/positions/**",
                                                                "/api/v1/areas",
                                                                "/api/v1/document-types/**",
                                                                "/api/v1/system-configurations/**")
                                                .hasAuthority("config:read")
                                                .pathMatchers("/api/v1/config/**", "/api/v1/positions/**",
                                                                "/api/v1/document-types/**",
                                                                "/api/v1/system-configurations/**")
                                                .hasAuthority("config:update")

                                                // ✅ MICROSERVICIO 04: PATRIMONIO - CATALOG (NUEVO)
                                                // Endpoints públicos de catálogo para el frontend
                                                .pathMatchers(HttpMethod.GET, "/api/v1/catalog/**")
                                                .authenticated()  // Solo requiere estar autenticado
                                                
                                                // MICROSERVICIO 04: PATRIMONIO - ASSETS (existente)
                                                .pathMatchers(HttpMethod.POST, "/api/v1/assets")
                                                .hasAnyRole("PATRIMONIO_GESTOR", "PATRIMONIO_OPERARIO")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/assets")
                                                .hasAnyRole("PATRIMONIO_GESTOR", "PATRIMONIO_OPERARIO",
                                                                "PATRIMONIO_VIEWER", "TENANT_ADMIN",
                                                                "MOVIMIENTOS_SOLICITANTE", "MOVIMIENTOS_APROBADOR",
                                                                "INVENTARIO_COORDINADOR", "INVENTARIO_VERIFICADOR")
                                                .pathMatchers(HttpMethod.PATCH, "/api/v1/assets/*/status")
                                                .hasAnyRole("PATRIMONIO_GESTOR", "PATRIMONIO_OPERARIO")
                                                .pathMatchers("/api/v1/assets/**", "/api/v1/depreciations/**",
                                                                "/api/v1/asset-disposals/**",
                                                                "/api/v1/asset-disposal-details/**")
                                                .hasRole("PATRIMONIO_GESTOR")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/asset-disposals")
                                                .hasAnyRole("PATRIMONIO_GESTOR", "AUDITORIA_VIEWER")

                                                // MICROSERVICIO 05: MOVIMIENTOS
                                                .pathMatchers(HttpMethod.POST, "/api/v1/asset-movements")
                                                .hasAuthority("movimientos:create")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/asset-movements/municipality/**")
                                                .hasAuthority("movimientos:read")
                                                .pathMatchers(HttpMethod.GET,
                                                                "/api/v1/asset-movements/pending-approval/municipality/**")
                                                .hasAuthority("movimientos:read")
                                                .pathMatchers("/api/v1/asset-movements/*/approve/municipality/**",
                                                                "/api/v1/asset-movements/*/reject/municipality/**",
                                                                "/api/v1/asset-movements/*/complete/municipality/**")
                                                .hasAnyAuthority("movimientos:approve", "movimientos:reject")
                                                .pathMatchers("/api/v1/handover-receipts/**")
                                                .hasAuthority("movimientos:acta:generate")

                                                // MICROSERVICIO 06: INVENTARIOS
                                                .pathMatchers(HttpMethod.POST, "/api/v1/inventories").hasAuthority("inventario:create")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/inventories").hasAuthority("inventario:read")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/inventories/with-details").hasAuthority("inventario:read")
                                                .pathMatchers(HttpMethod.PUT, "/api/v1/inventories/*/start").hasAuthority("inventario:update")
                                                .pathMatchers(HttpMethod.PUT, "/api/v1/inventories/*/complete").hasAuthority("inventario:close")
                                                .pathMatchers("/api/v1/inventory-details/**").hasAuthority("inventario:verify")

                                                // MICROSERVICIO 07: MANTENIMIENTO
                                                .pathMatchers(HttpMethod.POST, "/api/v1/maintenances")
                                                .hasAuthority("mantenimiento:create")
                                                .pathMatchers(HttpMethod.GET, "/api/v1/maintenances")
                                                .hasAuthority("mantenimiento:read")
                                                .pathMatchers("/api/v1/maintenances/**").authenticated()

                                                // TODO LO DEMÁS REQUIERE AUTENTICACIÓN
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                keycloakJwtAuthenticationConverter)))
                                .build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Procesar orígenes permitidos desde la configuración
                if ("*".equals(allowedOrigins)) {
                        config.addAllowedOriginPattern("*");
                } else {
                        List<String> origins = Arrays.asList(allowedOrigins.split(","));
                        origins.forEach(config::addAllowedOrigin);
                }

                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                config.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}