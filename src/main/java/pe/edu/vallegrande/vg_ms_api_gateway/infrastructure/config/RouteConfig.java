package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Las rutas están configuradas principalmente en application.yaml
                // Se pueden añadir rutas programáticas aquí si es necesario
                .build();
    }
}
