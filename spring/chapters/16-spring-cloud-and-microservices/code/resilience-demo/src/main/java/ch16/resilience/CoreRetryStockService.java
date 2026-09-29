package ch16.resilience;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

/** The same retry, written with Spring Framework 7's built-in annotations (no extra library). */
@Service
public class CoreRetryStockService {

    private final RemoteInventory remote;

    public CoreRetryStockService(RemoteInventory remote) {
        this.remote = remote;
    }

    // maxRetries = 2 -> up to 3 attempts in total (same as Resilience4j max-attempts: 3)
    @Retryable(includes = RemoteCallException.class, maxRetries = 2, delay = 20, multiplier = 2)
    @ConcurrencyLimit(limit = 10, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)   // a simple bulkhead
    public String stock(String sku) {
        return "live stock " + remote.stock(sku);
    }
}
