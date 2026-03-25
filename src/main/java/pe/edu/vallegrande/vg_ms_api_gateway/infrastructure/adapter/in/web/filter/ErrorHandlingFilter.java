package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web.dto.ErrorResponseDto;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * GlobalFilter que captura excepciones no manejadas en la cadena de filtros
 * y devuelve una respuesta de error estandarizada en JSON.
 * Se ejecuta con la máxima prioridad (Integer.MIN_VALUE) para envolver
 * toda la cadena.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorHandlingFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> handleError(exchange, throwable));
    }

    private Mono<Void> handleError(ServerWebExchange exchange, Throwable throwable) {
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        HttpStatus status;
        String message;

        if (throwable instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            message = rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred in the gateway";
            log.error("[GATEWAY] Unhandled error on {} {}: {}", exchange.getRequest().getMethod(), path,
                    throwable.getMessage(), throwable);
        }

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .correlationId(correlationId)
                .build();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            bytes = ("{\"error\":\"Internal Server Error\"}").getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
