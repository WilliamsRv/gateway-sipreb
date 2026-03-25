package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RequestContext;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.ProxyPort;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReactiveProxyAdapter implements ProxyPort {

    @Override
    public Mono<Void> forward(RequestContext context) {
        // Esta es una implementación simplificada.
        // En una arquitectura Spring Cloud Gateway real, esto lo maneja el
        // GatewayFilter.
        return Mono.empty();
    }
}
