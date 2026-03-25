package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out;

import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.Route;
import reactor.core.publisher.Flux;

public interface RouteRepositoryPort {
    Flux<Route> findAll();
}
