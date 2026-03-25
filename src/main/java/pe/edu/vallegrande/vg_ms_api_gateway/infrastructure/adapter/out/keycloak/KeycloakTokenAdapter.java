package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.AuthToken;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.TokenValidationPort;
import reactor.core.publisher.Mono;

@Primary
@Component
public class KeycloakTokenAdapter implements TokenValidationPort {

    private final WebClient webClient;
    
    // Obtenemos la URL del auth-service mediante variable de entorno o tomamos el valor por defecto
    @Value("${AUTH_SERVICE_URL:http://localhost:5002}")
    private String authServiceUrl;

    public KeycloakTokenAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Boolean> isValid(AuthToken token) {
        // Enviar el token para que sea validado por el Microservicio de Autenticación
        return webClient.post()
                .uri(authServiceUrl + "/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token.getValue())
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> Mono.just(false));
    }
}
