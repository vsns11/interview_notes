package ch09.cache;

import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/** A clock the test moves by hand, so TTL can be tested without sleeping. */
class FakeTicker implements Ticker {
    private final AtomicLong nanos = new AtomicLong();

    @Override public long read() { return nanos.get(); }

    void advance(Duration d) { nanos.addAndGet(d.toNanos()); }
}
