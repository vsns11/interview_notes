package ch15.obs;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@AutoConfigureMetrics
@AutoConfigureTracing
@ExtendWith(OutputCaptureExtension.class)
@org.junit.jupiter.api.Order(2)
class TracingTests {

    @Autowired RestTestClient client;
    @Autowired PrintingSpanExporter exporter;
    @Autowired SdkTracerProvider tracerProvider;

    /** Flush spans still sitting in the batch processor, then forget them. */
    private void startClean() {
        tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);
        exporter.clear();
    }

    /** The server span ends just after the response is written, so wait (max 2 s) until it is exported. */
    private List<SpanData> spansOnceFinished(String rootSpanName) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            tracerProvider.forceFlush().join(5, TimeUnit.SECONDS);   // push spans out of the batch processor
            if (exporter.finished().stream().anyMatch(s -> s.getName().equals(rootSpanName))) break;
            Thread.sleep(50);
        }
        return exporter.finished().stream()
                .sorted(Comparator.comparing(SpanData::getStartEpochNanos)).toList();
    }

    @Test
    void oneRequestOneTraceAcrossTwoServices(CapturedOutput output) throws InterruptedException {
        startClean();
        String body = client.get().uri("/orders/1/price").exchange()
                .expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody();
        List<SpanData> spans = spansOnceFinished("http get /orders/{id}/price");

        System.out.println("response: " + body);
        System.out.println("log lines (trace id - span id in brackets):");
        output.getOut().lines()
                .filter(l -> l.contains("asking pricing") || l.contains("pricing order"))
                .map(l -> l.substring(l.indexOf("[orders]")))
                .forEach(l -> System.out.println("  " + l));

        System.out.println("finished spans:");
        spans.forEach(s -> System.out.printf("  %-28s trace=%s span=%s parent=%s%n",
                s.getName(), s.getTraceId(), s.getSpanId(), s.getParentSpanId()));

        assertThat(spans).extracting(SpanData::getTraceId).containsOnly(spans.getFirst().getTraceId());
        assertThat(spans).hasSize(3);   // server span, client span, downstream server span
    }

    @Test
    void observedMethodBecomesASpan() throws InterruptedException {
        startClean();
        client.get().uri("/orders/2").exchange().expectStatus().isOk();
        List<String> names = spansOnceFinished("http get /orders/{id}").stream().map(SpanData::getName).toList();
        System.out.println("spans for GET /orders/2 -> " + names);
        assertThat(names).contains("order-lookup");
    }
}
