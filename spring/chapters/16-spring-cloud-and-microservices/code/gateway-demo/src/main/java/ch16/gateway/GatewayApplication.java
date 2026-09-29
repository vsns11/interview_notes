package ch16.gateway;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@SpringBootApplication
@RestController
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    /** Route 2, in the Java DSL: /api/inventory/** -> inventory service, protected by a circuit breaker. */
    @Bean
    RouterFunction<ServerResponse> inventoryRoute(@Value("${services.inventory.url}") String inventoryUrl) {
        return route("inventory")
                .GET("/api/inventory/**", http())
                .before(uri(inventoryUrl))
                .filter(circuitBreaker("inventoryCB", URI.create("forward:/fallback/inventory")))
                .build();
    }

    /** Answer used when the inventory service fails or its circuit is open. */
    @GetMapping("/fallback/inventory")
    String inventoryFallback() {
        return "{\"items\":[],\"note\":\"inventory temporarily unavailable\"}";
    }
}
