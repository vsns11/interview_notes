package ch16.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GatewayRoutingTests {

    /** A tiny stand-in "orders service" on a random port: echoes what it received. */
    static final HttpServer orders = startOrdersService();

    static HttpServer startOrdersService() {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            s.createContext("/", ex -> {
                String body = "orders-service saw " + ex.getRequestMethod() + " " + ex.getRequestURI()
                        + " X-Gateway=" + ex.getRequestHeaders().getFirst("X-Gateway");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, bytes.length);
                ex.getResponseBody().write(bytes);
                ex.close();
            });
            s.start();
            return s;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void services(DynamicPropertyRegistry registry) {
        registry.add("services.orders.url", () -> "http://localhost:" + orders.getAddress().getPort());
        registry.add("services.inventory.url", () -> "http://localhost:1");   // nothing listens there
    }

    @AfterAll
    static void stop() { orders.stop(0); }

    @Autowired RestTestClient client;

    @Test
    void yamlRouteStripsPrefixAndAddsHeaders() {
        var result = client.get().uri("/api/orders/7?expand=items").exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult();
        System.out.println("GET /api/orders/7 -> " + result.getResponseBody()
                + " | response X-Served-Via=" + result.getResponseHeaders().getFirst("X-Served-Via"));
        assertThat(result.getResponseBody()).isEqualTo("orders-service saw GET /orders/7?expand=items X-Gateway=ch16-gateway");
    }

    @Test
    void unmatchedPathIs404() {
        client.delete().uri("/api/orders/7").exchange().expectStatus().isNotFound();
        client.get().uri("/api/unknown").exchange().expectStatus().isNotFound();
        System.out.println("DELETE /api/orders/7 and GET /api/unknown -> 404 (no route matches)");
    }

    @Test
    void deadServiceFallsBackThroughCircuitBreaker() {
        String body = client.get().uri("/api/inventory/items").exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        System.out.println("GET /api/inventory/items (service down) -> " + body);
        assertThat(body).contains("temporarily unavailable");
    }
}
