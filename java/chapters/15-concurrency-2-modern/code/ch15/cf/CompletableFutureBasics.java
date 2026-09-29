package ch15.cf;

import java.util.List;
import java.util.concurrent.*;

/** Creating, transforming, composing and combining CompletableFutures. */
public class CompletableFutureBasics {

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CompletionException(e); }
    }

    // Simulated remote calls
    static int fetchUserId(String name)   { sleep(100); return name.length() * 7; }
    static String fetchProfile(int id)    { sleep(100); return "profile#" + id; }
    static double fetchPrice(String shop, int delayMs, double price) { sleep(delayMs); return price; }

    public static void main(String[] args) throws Exception {
        try (ExecutorService io = Executors.newFixedThreadPool(8)) {

            // 1) Start async work; the caller continues immediately
            CompletableFuture<Integer> idF = CompletableFuture.supplyAsync(() -> fetchUserId("grace"), io);
            System.out.println("1 submitted, done yet? " + idF.isDone());

            // 2) thenApply = map the result (sync function);  thenAccept = consume it
            CompletableFuture<String> upper = idF.thenApply(id -> "user-" + id).thenApply(String::toUpperCase);
            System.out.println("2 thenApply chain -> " + upper.join());

            // 3) thenCompose = flatMap: the next step is itself async (returns a CompletableFuture)
            CompletableFuture<CompletableFuture<String>> nested = idF.thenApply(id -> CompletableFuture.supplyAsync(() -> fetchProfile(id), io));
            CompletableFuture<String> flat = idF.thenCompose(id -> CompletableFuture.supplyAsync(() -> fetchProfile(id), io));
            System.out.println("3 thenApply gives a nested future: " + nested.join().getClass().getSimpleName()
                    + "; thenCompose gives the value: " + flat.join());

            // 4) thenCombine: two independent futures -> one result
            CompletableFuture<Double> price = CompletableFuture.supplyAsync(() -> fetchPrice("shop", 150, 20.0), io);
            CompletableFuture<Double> taxRate = CompletableFuture.supplyAsync(() -> { sleep(100); return 0.25; }, io);
            System.out.println("4 thenCombine -> total = " + price.thenCombine(taxRate, (p, t) -> p * (1 + t)).join());

            // 5) allOf: fan out to many shops in parallel, then gather. Time ~ slowest, not sum.
            long t0 = System.nanoTime();
            List<CompletableFuture<Double>> quotes = List.of(
                    CompletableFuture.supplyAsync(() -> fetchPrice("A", 300, 19.99), io),
                    CompletableFuture.supplyAsync(() -> fetchPrice("B", 200, 17.49), io),
                    CompletableFuture.supplyAsync(() -> fetchPrice("C", 250, 18.25), io));
            CompletableFuture<Void> all = CompletableFuture.allOf(quotes.toArray(CompletableFuture[]::new));
            double best = all.thenApply(v -> quotes.stream().mapToDouble(CompletableFuture::join).min().orElseThrow()).join();
            long ms = (System.nanoTime() - t0) / 1_000_000;
            System.out.println("5 allOf best price = " + best + ", took ~" + (ms / 100 * 100)
                    + " ms (sequential would be ~750)   [timing machine-dependent]");

            // 6) anyOf: first to finish wins (result type is Object)
            Object first = CompletableFuture.anyOf(
                    CompletableFuture.supplyAsync(() -> fetchPrice("slow", 300, 1.0), io),
                    CompletableFuture.supplyAsync(() -> fetchPrice("fast", 50, 2.0), io)).join();
            System.out.println("6 anyOf -> " + first);

            // 7) Callbacks: thenAccept / thenRun return CompletableFuture<Void>
            CompletableFuture.completedFuture("cached").thenAccept(v -> System.out.println("7 thenAccept got " + v))
                    .thenRun(() -> System.out.println("7 thenRun: nothing to receive, just runs after")).join();

            // 8) A CompletableFuture is also a promise you complete yourself
            CompletableFuture<String> promise = new CompletableFuture<>();
            io.submit(() -> { sleep(50); promise.complete("completed by another thread"); });
            System.out.println("8 manual complete -> " + promise.get(1, TimeUnit.SECONDS));

            // 9) Which thread runs a non-async stage? The one that completed the previous stage, or the caller
            //    if it was already complete. Use the *Async variants to choose an executor explicitly.
            String where = CompletableFuture.supplyAsync(() -> 1, io)
                    .thenApplyAsync(x -> Thread.currentThread().getName().startsWith("pool-") ? "our pool" : "elsewhere", io).join();
            System.out.println("9 thenApplyAsync(fn, io) ran on " + where);
        }
    }
}
