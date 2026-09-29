package ch14.scheduling;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Jobs {
    /** Start time (ms) of every run, plus the thread it ran on. */
    public record Run(long startMillis, String thread) {}

    public final List<Run> rateRuns = new CopyOnWriteArrayList<>();
    public final List<Run> delayRuns = new CopyOnWriteArrayList<>();
    public final List<String> cronRuns = new CopyOnWriteArrayList<>();

    private static Run now() { return new Run(System.currentTimeMillis(), Thread.currentThread().getName()); }

    private static void work(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // both jobs "work" for 200 ms; both have a period of 300 ms

    @Scheduled(fixedRate = 300)           // measured from START to START
    void fixedRateJob() {
        rateRuns.add(now());
        work(200);
    }

    @Scheduled(fixedDelay = 300, initialDelay = 50)   // measured from END to next START
    void fixedDelayJob() {
        delayRuns.add(now());
        work(200);
    }

    @Scheduled(cron = "*/1 * * * * *", zone = "UTC")  // sec min hour day month weekday: every second, on the second
    void cronJob() {
        cronRuns.add(LocalTime.now().withNano(0) + " on " + Thread.currentThread().getName());
    }
}
