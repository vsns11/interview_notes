package ch15.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@ExtendWith(OutputCaptureExtension.class)
class GracefulShutdownTests {

    private final HttpClient http = HttpClient.newHttpClient();

    /** Starts the app, begins a 1-second request, closes the context while it runs. */
    private String shutdownDuringRequest(String shutdownMode) throws Exception {
        RuntimeApplication.slowStarted = new CountDownLatch(1);
        ConfigurableApplicationContext ctx = SpringApplication.run(RuntimeApplication.class,
                "--server.port=0", "--server.shutdown=" + shutdownMode, "--spring.main.banner-mode=off");
        int port = ((WebServerApplicationContext) ctx).getWebServer().getPort();

        CompletableFuture<String> inFlight = http.sendAsync(
                        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/slow?ms=1000")).build(),
                        HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> "HTTP " + r.statusCode() + " '" + r.body() + "'")
                .exceptionally(e -> "FAILED: " + e.getCause().getClass().getSimpleName());
        RuntimeApplication.slowStarted.await(5, TimeUnit.SECONDS);   // request is now inside the controller

        long start = System.nanoTime();
        Thread closer = Thread.ofPlatform().start(ctx::close);        // like SIGTERM from Kubernetes
        Thread.sleep(200);
        String newRequest;
        try {
            http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/thread")).build(),
                    HttpResponse.BodyHandlers.ofString());
            newRequest = "accepted";
        } catch (Exception e) {
            newRequest = "rejected (" + e.getClass().getSimpleName() + ")";
        }
        closer.join();
        long closeMs = (System.nanoTime() - start) / 1_000_000;
        String result = inFlight.get(5, TimeUnit.SECONDS);
        System.out.printf("server.shutdown=%-9s in-flight -> %s | new request during shutdown -> %s | close took %s%n",
                shutdownMode, result, newRequest, closeMs >= 700 ? "~1 s (waited)" : "< 0.7 s (did not wait)");
        return result;
    }

    @Test
    void gracefulLetsInFlightRequestsFinish(CapturedOutput output) throws Exception {
        String result = shutdownDuringRequest("graceful");
        assertThat(result).isEqualTo("HTTP 200 'done after 1000 ms'");
        output.getOut().lines().filter(l -> l.contains("raceful shutdown"))
                .map(l -> "  log: " + l.substring(l.indexOf(": ") + 2))
                .forEach(System.out::println);
    }

    @Test
    void immediateDropsThem() throws Exception {
        String result = shutdownDuringRequest("immediate");
        assertThat(result).startsWith("FAILED");
    }
}
