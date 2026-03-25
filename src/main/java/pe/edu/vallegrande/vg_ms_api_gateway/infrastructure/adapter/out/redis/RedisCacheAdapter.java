package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.CachePort;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Profile("prod")   // Solo activo en producción — requiere Redis corriendo
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> put(String key, String value, Duration ttl) {
        return reactiveRedisTemplate.opsForValue().set(key, value, ttl).then();
    }

    @Override
    public Mono<String> get(String key) {
        return reactiveRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Mono<Long> increment(String key, Duration ttl) {
        return reactiveRedisTemplate.opsForValue().increment(key)
                .flatMap(count -> reactiveRedisTemplate.expire(key, ttl)
                        .then(Mono.just(count)));
    }
}
