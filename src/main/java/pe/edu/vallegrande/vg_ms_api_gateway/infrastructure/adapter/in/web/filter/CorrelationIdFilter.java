package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtro global para asignar un Correlation ID a cada petición.
 * Permite rastrear una petición a través de múltiples microservicios.
 */
@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        
        // 1. Obtener el ID existente o generar uno nuevo
        String correlationId = headers.getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation ID: {}", correlationId);
        } else {
            log.debug("Found existing Correlation ID: {}", correlationId);
        }

        // 2. Agregar el ID a la RESPUESTA para que el cliente lo vea en caso de error
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        // 3. Propagar el ID al microservicio (DOWNSTREAM) mutando el request
        return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(CORRELATION_ID_HEADER, correlationId)
                        .build())
                .build());
    }

    @Override
    public int getOrder() {
        // Ejecutar muy temprano (incluso antes del Rate Limit)
        return -100;
    }
}
