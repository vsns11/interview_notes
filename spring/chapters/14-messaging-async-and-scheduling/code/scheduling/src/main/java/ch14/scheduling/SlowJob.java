package ch14.scheduling;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** A fixed-rate job that is SLOWER than its period (250 ms of work every 100 ms). Off unless app.slow-job.enabled=true. */
@Component
@ConditionalOnBooleanProperty("app.slow-job.enabled")
public class SlowJob {
    private final AtomicInteger running = new AtomicInteger();
    public final AtomicInteger maxRunningAtOnce = new AtomicInteger();
    public final AtomicInteger runs = new AtomicInteger();
    public volatile String thread = "";

    @Scheduled(fixedRate = 100)
    void tick() throws InterruptedException {
        int now = running.incrementAndGet();
        maxRunningAtOnce.accumulateAndGet(now, Math::max);
        runs.incrementAndGet();
        thread = Thread.currentThread().isVirtual() ? "virtual thread " + Thread.currentThread().getName() : Thread.currentThread().getName();
        try { Thread.sleep(250); } finally { running.decrementAndGet(); }
    }
}
