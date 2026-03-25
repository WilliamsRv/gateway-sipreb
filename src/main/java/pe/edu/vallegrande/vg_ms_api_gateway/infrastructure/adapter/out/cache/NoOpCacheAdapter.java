package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.CachePort;
import reactor.core.publisher.Mono;

import java.time.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

/**
 * Implementación en memoria del CachePort para desarrollo local.
 */
@Component
@Profile("!prod")
public class NoOpCacheAdapter implements CachePort {
    
    // Almacena contadores por clave
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    // Almacena el timestamp de expiración por clave
    private final ConcurrentHashMap<String, Instant> expirationMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> put(String key, String value, Duration ttl) {
        return Mono.empty(); 
    }

    @Override
    public Mono<String> get(String key) {
        return Mono.empty();
    }

    @Override
    public Mono<Long> increment(String key, Duration ttl) {
        Instant now = Instant.now();
        
        // Limpiar si ya expiró
        if (expirationMap.containsKey(key) && expirationMap.get(key).isBefore(now)) {
            counters.remove(key);
            expirationMap.remove(key);
        }

        // Si es la primera vez (o tras expirar), establecemos la expiración
        expirationMap.putIfAbsent(key, now.plus(ttl));
        
        AtomicLong counter = counters.computeIfAbsent(key, k -> new AtomicLong(0));
        return Mono.just(counter.incrementAndGet());
    }
}
