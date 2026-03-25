package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.AuthToken;
import reactor.core.publisher.Mono;

public interface ValidateTokenUseCase {
    Mono<Boolean> validate(AuthToken token);
}
