package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class GatewayController {

    private static final Pattern PATH_PATTERN = Pattern.compile("\\[([^\\]]+)\\]");
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    private final RouteLocator routeLocator;

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of("status", "UP", "message", "API Gateway is running"));
    }

    /**
     * Lista todas las rutas configuradas en el Gateway.
     */
    @GetMapping("/gateway/routes")
    public Flux<Map<String, Object>> listRoutes() {
        return routeLocator.getRoutes()
                .map(route -> Map.<String, Object>of(
                        "id", route.getId(),
                        "uri", route.getUri().toString(),
                        "order", route.getOrder(),
                        "predicates", route.getPredicate().toString(),
                        "filters", route.getFilters().stream()
                                .map(f -> f.getClass().getSimpleName())
                                .toList()
                ));
    }

    /**
     * Prueba qué rutas coinciden con un path determinado usando AntPathMatcher.
     *
     * @param path El path a verificar (e.g. /api/v1/assets/123)
     */
    @GetMapping("/gateway/routes/match")
    public Flux<Map<String, Object>> matchRoute(@RequestParam String path) {
        if (path == null || path.isBlank()) {
            return Flux.empty();
        }
        return routeLocator.getRoutes()
                .filter(route -> routeMatchesPath(route, path))
                .map(route -> Map.<String, Object>of(
                        "id", route.getId(),
                        "uri", route.getUri().toString(),
                        "order", route.getOrder(),
                        "predicate", route.getPredicate().toString()
                ));
    }

    /**
     * Devuelve información de depuración sobre la configuración del Gateway.
     */
    @GetMapping("/gateway/debug")
    public Mono<Map<String, Object>> debug() {
        return routeLocator.getRoutes()
                .collectList()
                .map(routes -> Map.<String, Object>of(
                        "totalRoutes", routes.size(),
                        "routeIds", routes.stream().map(Route::getId).toList(),
                        "gatewayStatus", "RUNNING"
                ));
    }

    /**
     * Extracts path patterns from a route's predicate toString() output and
     * tests the given path against them using AntPathMatcher.
     * Predicate toString() format example: "[/api/v1/assets/**, /api/v1/depreciations/**]"
     */
    private boolean routeMatchesPath(Route route, String path) {
        String predicateStr = route.getPredicate().toString();
        Matcher matcher = PATH_PATTERN.matcher(predicateStr);
        while (matcher.find()) {
            String[] patterns = matcher.group(1).split(",\\s*");
            for (String pattern : patterns) {
                if (ANT_PATH_MATCHER.match(pattern.trim(), path)) {
                    return true;
                }
            }
        }
        return false;
    }
}

