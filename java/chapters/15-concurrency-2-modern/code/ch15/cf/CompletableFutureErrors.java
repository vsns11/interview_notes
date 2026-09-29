package ch15.cf;

import java.util.concurrent.*;

/** Error handling and timeouts in CompletableFuture pipelines. */
public class CompletableFutureErrors {

    static int parse(String s) { return Integer.parseInt(s); }

    public static void main(String[] args) throws Exception {
        // 1) An exception skips the following thenApply stages and travels down the chain
        CompletableFuture<Integer> failed = CompletableFuture.supplyAsync(() -> parse("x"))
                .thenApply(n -> { System.out.println("never runs"); return n * 2; });

        // join() wraps it in CompletionException, get() in ExecutionException
        try { failed.join(); }
        catch (CompletionException e) { System.out.println("1 join -> CompletionException, cause " + e.getCause().getClass().getSimpleName()); }
        try { failed.get(); }
        catch (ExecutionException e) { System.out.println("1 get  -> ExecutionException,  cause " + e.getCause().getClass().getSimpleName()); }
        System.out.println("1 isCompletedExceptionally = " + failed.isCompletedExceptionally() + ", state = " + failed.state());

        // 2) exceptionally: recover with a fallback value (like catch)
        int recovered = CompletableFuture.supplyAsync(() -> parse("x"))
                .exceptionally(ex -> { System.out.println("2 exceptionally sees " + ex.getClass().getSimpleName()
                        + " wrapping " + ex.getCause().getClass().getSimpleName()); return -1; })
                .join();
        System.out.println("2 recovered value = " + recovered);

        // 3) handle: always runs, gets (result, exception) - one of them is null
        for (String in : new String[]{"21", "oops"}) {
            String r = CompletableFuture.supplyAsync(() -> parse(in))
                    .handle((val, ex) -> ex == null ? "ok " + val * 2 : "failed: " + ex.getCause().getMessage())
                    .join();
            System.out.println("3 handle(" + in + ") -> " + r);
        }

        // 4) whenComplete: side effect (logging); the result or exception passes through unchanged
        CompletableFuture<Integer> logged = CompletableFuture.supplyAsync(() -> parse("oops"))
                .whenComplete((val, ex) -> System.out.println("4 whenComplete logs: " + (ex != null ? "error" : val)));
        System.out.println("4 still failed afterwards? " + logged.handle((v, ex) -> ex != null).join());

        // 5) Timeouts (Java 9): fail, or substitute a default
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> { sleep(2_000); return "late"; });
        try {
            slow.orTimeout(100, TimeUnit.MILLISECONDS).join();
        } catch (CompletionException e) {
            System.out.println("5 orTimeout -> " + e.getCause().getClass().getSimpleName());
        }
        String dflt = CompletableFuture.supplyAsync(() -> { sleep(2_000); return "late"; })
                .completeOnTimeout("default", 100, TimeUnit.MILLISECONDS).join();
        System.out.println("5 completeOnTimeout -> " + dflt);

        // 6) exceptionallyCompose (Java 12): recover by starting another async call (e.g. a backup server)
        String viaBackup = CompletableFuture.<String>failedFuture(new IllegalStateException("primary down"))
                .exceptionallyCompose(ex -> CompletableFuture.supplyAsync(() -> "from backup"))
                .join();
        System.out.println("6 exceptionallyCompose -> " + viaBackup);

        // 7) Forgotten failures: nobody calls join/get/exceptionally -> the error is silently lost
        CompletableFuture.runAsync(() -> { throw new IllegalStateException("nobody will ever see this"); });
        System.out.println("7 fire-and-forget failure printed nothing (that's the danger)");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
