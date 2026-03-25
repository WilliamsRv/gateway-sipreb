package pe.edu.vallegrande.vg_ms_api_gateway.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.RateLimitResult;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.RateLimitUseCase;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.CachePort;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService implements RateLimitUseCase {

    private final CachePort cachePort;
    
    @Value("${ratelimit.max-requests:100}")
    private long maxRequests;

    @Value("${ratelimit.window-seconds:60}")
    private long windowSeconds;

    @Value("${ratelimit.authenticated-max-requests:10000}")
    private long authenticatedMaxRequests;

    @Override
    public Mono<RateLimitResult> checkLimit(String identifier, boolean isAuthenticated) {
        String key = "ratelimit:" + identifier;
        long limit = isAuthenticated ? authenticatedMaxRequests : maxRequests;
        return cachePort.increment(key, Duration.ofSeconds(windowSeconds))
                .map(count -> RateLimitResult.builder()
                        .allowed(count <= limit)
                        .limit(limit)
                        .remaining(Math.max(0, limit - count))
                        .build());
    }
}
