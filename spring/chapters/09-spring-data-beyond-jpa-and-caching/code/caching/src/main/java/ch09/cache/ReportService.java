package ch09.cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** Cache stampede demo: an expensive report requested by many threads at the same moment. */
@Service
public class ReportService {

    public static final int CALLERS = 8;
    private final AtomicInteger plainComputations = new AtomicInteger();
    private final AtomicInteger syncComputations = new AtomicInteger();
    private volatile CountDownLatch plainLatch = new CountDownLatch(CALLERS);
    private volatile CountDownLatch syncLatch = new CountDownLatch(CALLERS);

    @Cacheable("reports-plain")                       // every thread that misses computes the report
    public String plain(String day) throws InterruptedException {
        plainComputations.incrementAndGet();
        slowWork(plainLatch);
        return "report " + day;
    }

    @Cacheable(cacheNames = "reports-sync", sync = true)   // one thread computes, the others wait for it
    public String synced(String day) throws InterruptedException {
        syncComputations.incrementAndGet();
        slowWork(syncLatch);
        return "report " + day;
    }

    // Read counters through methods: this bean is a CGLIB proxy, and a field read on the proxy
    // would see the proxy's own (null) field, not the target's - see chapter 4.
    public int plainComputations() { return plainComputations.get(); }
    public int syncComputations() { return syncComputations.get(); }

    /** "Expensive" work: waits until all callers are inside the method, or 300 ms at most. */
    private static void slowWork(CountDownLatch latch) throws InterruptedException {
        latch.countDown();
        latch.await(300, TimeUnit.MILLISECONDS);
    }
}
