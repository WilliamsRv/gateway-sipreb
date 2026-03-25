package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RequestContext;
import reactor.core.publisher.Mono;

public interface ProxyPort {
    Mono<Void> forward(RequestContext context);
}
