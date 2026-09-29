package ch13.load;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Closed-loop load: `concurrency` virtual-thread clients, each sending requests back-to-back
 * until `total` requests are done. Runs in the same JVM as the server (numbers are machine-dependent).
 */
public final class LoadGenerator {
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .executor(Executors.newVirtualThreadPerTaskExecutor())   // the client itself adds no platform threads
            .build();

    public record Result(String mode, int concurrency, int requests, long wallMs, long p50, long p99, int peakThreads, int errors) {
        public long rps() { return requests * 1000L / Math.max(1, wallMs); }
        @Override public String toString() {
            return String.format("| %-28s | %5d | %6d | %7d | %6d | %6d | %6d | %21d | %6d |",
                    mode, concurrency, requests, wallMs, rps(), p50, p99, peakThreads, errors);
        }
    }

    public static String header() {
        return "| mode                         | conc. |   reqs | wall ms |  req/s | p50 ms | p99 ms | peak platform threads | errors |";
    }

    public Result run(String mode, String url, int concurrency, int total) throws Exception {
        AtomicInteger next = new AtomicInteger(), errors = new AtomicInteger();
        long[] latencies = new long[total];
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        THREADS.resetPeakThreadCount();
        long t0 = System.nanoTime();
        try (ExecutorService clients = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> fs = new ArrayList<>();
            for (int c = 0; c < concurrency; c++) {
                fs.add(clients.submit(() -> {
                    int i;
                    while ((i = next.getAndIncrement()) < total) {
                        long s = System.nanoTime();
                        try {
                            HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
                            if (r.statusCode() != 200) errors.incrementAndGet();
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                        latencies[i] = (System.nanoTime() - s) / 1_000_000;
                    }
                    return null;
                }));
            }
            for (Future<?> f : fs) f.get();
        }
        long wall = (System.nanoTime() - t0) / 1_000_000;
        Arrays.sort(latencies);
        return new Result(mode, concurrency, total, wall,
                latencies[total / 2], latencies[(int) (total * 0.99)], THREADS.getPeakThreadCount(), errors.get());
    }
}
