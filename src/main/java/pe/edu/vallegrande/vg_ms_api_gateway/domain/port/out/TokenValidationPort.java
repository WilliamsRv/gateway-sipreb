package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.AuthToken;
import reactor.core.publisher.Mono;

public interface TokenValidationPort {
    Mono<Boolean> isValid(AuthToken token);
}
