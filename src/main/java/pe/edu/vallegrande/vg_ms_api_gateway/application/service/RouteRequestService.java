package pe.edu.vallegrande.vg_ms_api_gateway.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RequestContext;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.RouteRequestUseCase;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.ProxyPort;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RouteRequestService implements RouteRequestUseCase {

    private final ProxyPort proxyPort;

    @Override
    public Mono<Void> route(RequestContext context) {
        return proxyPort.forward(context);
    }
}
