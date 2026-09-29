package ch16.resilience;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** Stands in for a remote HTTP service. Tests decide whether it fails. */
@Component
public class RemoteInventory {

    private final AtomicInteger calls = new AtomicInteger();
    private volatile boolean down;
    private final AtomicInteger failNext = new AtomicInteger();

    public int stock(String sku) {
        calls.incrementAndGet();
        if (down || failNext.getAndDecrement() > 0) {
            throw new RemoteCallException("inventory unavailable");
        }
        return 7;
    }

    public void setDown(boolean down) { this.down = down; }
    public void failNextCalls(int n) { failNext.set(n); }
    public int calls() { return calls.get(); }
    public void reset() { calls.set(0); down = false; failNext.set(0); }
}
