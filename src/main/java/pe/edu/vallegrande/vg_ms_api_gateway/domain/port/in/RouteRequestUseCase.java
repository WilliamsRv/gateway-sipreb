package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RequestContext;
import reactor.core.publisher.Mono;

public interface RouteRequestUseCase {
    Mono<Void> route(RequestContext context);
}
