package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RateLimitResult;
import reactor.core.publisher.Mono;

public interface RateLimitUseCase {
    Mono<RateLimitResult> checkLimit(String identifier, boolean isAuthenticated);
}
