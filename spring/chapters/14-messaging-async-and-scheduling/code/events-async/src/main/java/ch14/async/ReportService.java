package ch14.async;

import java.util.concurrent.CompletableFuture;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final Trace trace;

    public ReportService(Trace trace) { this.trace = trace; }

    @Async   // default executor: Boot's applicationTaskExecutor
    public CompletableFuture<String> build(String name) {
        return CompletableFuture.completedFuture(name + " built on " + Trace.thread()
                + " requestId=" + MDC.get("requestId"));
    }

    @Async("reportExecutor")   // pick a specific executor bean by name / qualifier
    public CompletableFuture<String> buildBig(String name) {
        return CompletableFuture.completedFuture(name + " built on " + Trace.thread()
                + " requestId=" + MDC.get("requestId"));
    }

    @Async
    public CompletableFuture<String> buildBroken(String name) {
        throw new IllegalStateException("no data for " + name);   // becomes an exceptionally completed future
    }

    @Async
    public void fireAndForget(String name) {
        throw new IllegalStateException("cannot export " + name);  // void: goes to AsyncUncaughtExceptionHandler
    }

    /** Self-invocation: the call below does not go through the proxy, so it is NOT asynchronous. */
    public String buildViaThis(String name) {
        return this.build(name).join();
    }
}
