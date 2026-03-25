package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.RateLimitUseCase;
import reactor.core.publisher.Mono;

import java.util.Objects;

import org.springframework.context.annotation.Profile;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final RateLimitUseCase rateLimitUseCase;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress()
                .getHostAddress();

        return exchange.getPrincipal()
                .flatMap(principal -> rateLimitUseCase.checkLimit(principal.getName(), true))
                .switchIfEmpty(Mono.defer(() -> rateLimitUseCase.checkLimit(clientIp, false)))
                .flatMap(result -> {
                    // Agregar cabeceras informativas
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(result.getLimit()));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));

                    if (result.isAllowed()) {
                        return chain.filter(exchange);
                    } else {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return -90; // Después del AuthFilter
    }
}
