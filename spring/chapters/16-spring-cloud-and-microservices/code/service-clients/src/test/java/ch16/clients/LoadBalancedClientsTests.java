package ch16.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class LoadBalancedClientsTests {

    // two instances of the "inventory" service, each saying who it is
    static final HttpServer instanceA = inventoryInstance("A");
    static final HttpServer instanceB = inventoryInstance("B");

    static HttpServer inventoryInstance(String name) {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            s.createContext("/stock/", ex -> {
                String sku = ex.getRequestURI().getPath().substring("/stock/".length());
                byte[] json = ("{\"sku\":\"" + sku + "\",\"quantity\":7,\"servedBy\":\"" + name + "\"}")
                        .getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, json.length);
                ex.getResponseBody().write(json);
                ex.close();
            });
            s.start();
            return s;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void instances(DynamicPropertyRegistry r) {
        r.add("spring.cloud.discovery.client.simple.instances.inventory[0].uri",
                () -> "http://localhost:" + instanceA.getAddress().getPort());
        r.add("spring.cloud.discovery.client.simple.instances.inventory[1].uri",
                () -> "http://localhost:" + instanceB.getAddress().getPort());
    }

    @AfterAll
    static void stop() { instanceA.stop(0); instanceB.stop(0); }

    @Autowired InventoryFeignClient feign;
    @Autowired InventoryHttpClient httpInterface;

    @Test
    void feignClientIsLoadBalanced() {
        List<String> servedBy = new ArrayList<>();
        for (int i = 0; i < 4; i++) servedBy.add(feign.stock("SKU-" + i).servedBy());
        System.out.println("OpenFeign      4 calls served by " + servedBy + "  5th call -> " + feign.stock("SKU-9"));
        assertThat(servedBy).containsOnly("A", "B").contains("A", "B");
    }

    @Test
    void httpInterfaceClientIsLoadBalanced() {
        List<String> servedBy = new ArrayList<>();
        for (int i = 0; i < 4; i++) servedBy.add(httpInterface.stock("SKU-" + i).servedBy());
        System.out.println("HTTP interface 4 calls served by " + servedBy);
        assertThat(servedBy).containsOnly("A", "B").contains("A", "B");
    }
}
