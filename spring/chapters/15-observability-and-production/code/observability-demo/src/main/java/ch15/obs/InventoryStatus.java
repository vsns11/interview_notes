package ch15.obs;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/** Pretend connection state of a downstream inventory system (tests flip it). */
@Component
public class InventoryStatus {
    private final AtomicBoolean reachable = new AtomicBoolean(true);

    public boolean reachable() { return reachable.get(); }
    public void setReachable(boolean value) { reachable.set(value); }
}
