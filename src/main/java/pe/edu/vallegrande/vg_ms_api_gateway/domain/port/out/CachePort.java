package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface CachePort {
    Mono<Void> put(String key, String value, Duration ttl);

    Mono<String> get(String key);

    Mono<Long> increment(String key, Duration ttl);
}
