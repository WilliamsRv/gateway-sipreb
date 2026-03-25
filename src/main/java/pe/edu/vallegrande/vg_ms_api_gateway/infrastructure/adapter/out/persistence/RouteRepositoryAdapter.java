package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.Route;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.RouteRepositoryPort;
import reactor.core.publisher.Flux;

@Component
public class RouteRepositoryAdapter implements RouteRepositoryPort {
    @Override
    public Flux<Route> findAll() {
        // En una implementación real esto podría venir de un JSON o Base de datos
        return Flux.empty();
    }
}
