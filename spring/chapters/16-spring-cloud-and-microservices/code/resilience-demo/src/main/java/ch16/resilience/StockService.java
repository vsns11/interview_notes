package ch16.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final RemoteInventory remote;

    public StockService(RemoteInventory remote) {
        this.remote = remote;
    }

    // Circuit breaker "inventory" (configured in application.yaml); on failure OR open circuit -> fallback
    @CircuitBreaker(name = "inventory", fallbackMethod = "cachedStock")
    public String stockWithBreaker(String sku) {
        return "live stock " + remote.stock(sku);
    }

    // fallback: same parameters + the exception as the last parameter
    private String cachedStock(String sku, Throwable t) {
        return "cached stock 5 (" + t.getClass().getSimpleName() + ")";
    }

    // Retry "inventory-retry": up to 3 attempts in total, exponential back-off
    @Retry(name = "inventory-retry")
    public String stockWithRetry(String sku) {
        return "live stock " + remote.stock(sku);
    }

    // Rate limiter "search": at most 3 calls per refresh period, extra callers rejected at once
    @RateLimiter(name = "search")
    public String search(String text) {
        return "results for " + text;
    }
}
