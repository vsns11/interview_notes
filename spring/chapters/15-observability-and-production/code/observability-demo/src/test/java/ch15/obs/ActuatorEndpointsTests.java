package ch15.obs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@AutoConfigureMetrics   // tests switch metrics export (e.g. Prometheus) off unless you ask for it
@AutoConfigureTracing   // same for span export
@org.junit.jupiter.api.Order(1)
class ActuatorEndpointsTests {

    @Autowired RestTestClient client;
    @Autowired InventoryStatus inventory;
    @Autowired ApplicationContext context;

    @AfterEach
    void reset() {
        inventory.setReachable(true);
        AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
    }

    private String get(String path, int expectedStatus, boolean asOps) {
        return client.get().uri(path)
                .headers(h -> { if (asOps) h.setBasicAuth("ops", "ops-secret"); })
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody(String.class).returnResult().getResponseBody();
    }

    @Test
    void healthHidesDetailsFromAnonymousCallers() {
        String anonymous = get("/actuator/health", 200, false);
        String ops = get("/actuator/health", 200, true);
        System.out.println("anonymous /actuator/health -> " + anonymous);
        System.out.println("ops       /actuator/health -> " + ops);
        assertThat(anonymous).isEqualTo("{\"groups\":[\"deploy\",\"liveness\",\"readiness\"],\"status\":\"UP\"}");
        assertThat(ops).contains("\"inventory\":{\"details\":{\"latencyMs\":12},\"status\":\"UP\"}");
    }

    @Test
    void probesAndCustomGroupReactToADownDependency() {
        System.out.println("inventory reachable:");
        System.out.println("  liveness  -> " + get("/actuator/health/liveness", 200, false));
        System.out.println("  readiness -> " + get("/actuator/health/readiness", 200, false));

        inventory.setReachable(false);
        System.out.println("inventory DOWN:");
        System.out.println("  liveness  -> " + get("/actuator/health/liveness", 200, false));
        System.out.println("  readiness -> " + get("/actuator/health/readiness", 503, false) + " (HTTP 503)");
        String deploy = get("/actuator/health/deploy", 503, false);
        System.out.println("  deploy    -> " + deploy + " (HTTP 503)");
        assertThat(deploy).contains("\"status\":\"DOWN\"");
    }

    @Test
    void appCanTakeItselfOutOfRotation() {
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
        String readiness = get("/actuator/health/readiness", 503, false);
        System.out.println("after REFUSING_TRAFFIC: readiness -> " + readiness + " (HTTP 503)");
        assertThat(readiness).contains("OUT_OF_SERVICE");
    }

    @Test
    void infoShowsBuildJavaAndEnvInfo() {
        String info = get("/actuator/info", 200, false);
        System.out.println("/actuator/info -> " + info.substring(0, Math.min(info.length(), 400)) + "...");
        assertThat(info).contains("\"description\":\"Orders demo for chapter 15\"")
                .contains("\"artifact\":\"ch15-observability-demo\"")
                .contains("\"java\":");
    }

    @Test
    void metricsAndPrometheusScrape() {
        get("/orders/1", 200, false);
        get("/orders/1", 200, false);
        get("/orders/99", 404, false);
        client.post().uri("/orders").body("monitor").exchange().expectStatus().isOk();

        String metric = get("/actuator/metrics/orders.lookups", 200, true);
        System.out.println("/actuator/metrics/orders.lookups -> " + metric);
        assertThat(metric).contains("\"name\":\"orders.lookups\"");

        String scrape = get("/actuator/prometheus", 200, false);
        System.out.println("/actuator/prometheus (selected lines):");
        Arrays.stream(scrape.split("\n"))
                .filter(l -> l.startsWith("orders_lookups_total")
                        || l.startsWith("orders_place_seconds_count")
                        || l.startsWith("orders_stored")
                        || l.startsWith("order_lookup_seconds_count")
                        || (l.startsWith("http_server_requests_seconds_count") && l.contains("/orders/{id}")))
                .sorted()
                .forEach(l -> System.out.println("  " + l));
        assertThat(scrape).contains("orders_lookups_total{region=\"eu-west-1\",result=\"found\"} 2.0");
    }

    @Test
    void sensitiveEndpointsNeedTheOpsRole() {
        get("/actuator/env", 401, false);
        String loggers = get("/actuator/loggers/ch15.obs", 200, true);
        System.out.println("anonymous /actuator/env -> 401, ops /actuator/loggers/ch15.obs -> " + loggers);
    }
}
