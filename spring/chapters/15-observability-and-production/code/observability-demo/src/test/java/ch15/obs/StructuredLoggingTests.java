package ch15.obs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.client.RestTestClient;

// Same app, but the console now prints one JSON object per log event (Elastic Common Schema)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "logging.structured.format.console=ecs",
        "logging.structured.ecs.service.version=1.0.0",
        "logging.structured.ecs.service.environment=test"
})
@AutoConfigureRestTestClient
@AutoConfigureTracing
@ExtendWith(OutputCaptureExtension.class)
@Order(3)
class StructuredLoggingTests {

    @Autowired RestTestClient client;

    @Test
    void logLinesAreJson(CapturedOutput output) {
        client.get().uri("/orders/1").exchange().expectStatus().isOk();
        String line = output.getOut().lines()
                .filter(l -> l.contains("looking up order 1")).findFirst().orElseThrow();
        System.out.println("ECS log line:");
        System.out.println(line);
        assertThat(line).startsWith("{\"@timestamp\"")
                .contains("\"message\":\"looking up order 1\"")
                .contains("\"traceId\"");
    }
}
