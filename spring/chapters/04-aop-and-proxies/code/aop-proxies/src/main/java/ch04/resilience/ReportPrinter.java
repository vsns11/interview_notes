package ch04.resilience;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Service;

/** A resource that must not be hit by more than 2 callers at a time. */
@Service
public class ReportPrinter {

    private final AtomicInteger inside = new AtomicInteger();
    private final AtomicInteger maxInside = new AtomicInteger();

    @ConcurrencyLimit(2)                        // default policy BLOCK: extra callers wait
    public void print(String doc) throws InterruptedException {
        int now = inside.incrementAndGet();
        maxInside.accumulateAndGet(now, Math::max);
        Thread.sleep(50);                        // hold the "printer" for a while
        inside.decrementAndGet();
    }

    @ConcurrencyLimit(limit = 1, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)  // 7.0.3+
    public void printUrgent(String doc) throws InterruptedException {
        Thread.sleep(100);
    }

    public int maxConcurrent() { return maxInside.get(); }
}
