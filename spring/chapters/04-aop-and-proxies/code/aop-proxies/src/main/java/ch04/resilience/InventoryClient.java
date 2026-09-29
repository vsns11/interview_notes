package ch04.resilience;

import java.util.ArrayList;
import java.util.List;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

/** Pretends to call a flaky remote inventory service. */
@Service
public class InventoryClient {

    private final List<String> attempts = new ArrayList<>();
    private int failuresLeft;

    public void failNextCalls(int n) { failuresLeft = n; attempts.clear(); }
    public List<String> attempts() { return List.copyOf(attempts); }

    // up to 3 retries (4 attempts in total), waiting 10 ms, then 20 ms, then 40 ms
    @Retryable(includes = TransientException.class, maxRetries = 3, delay = 10, multiplier = 2)
    public int stockFor(String sku) {
        attempts.add("attempt " + (attempts.size() + 1));
        if (failuresLeft-- > 0) {
            throw new TransientException("inventory timed out");
        }
        return 42;
    }

    // IllegalArgumentException is not in 'includes', so it is never retried
    @Retryable(includes = TransientException.class, maxRetries = 3, delay = 10)
    public int stockForBadSku(String sku) {
        attempts.add("attempt " + (attempts.size() + 1));
        throw new IllegalArgumentException("bad sku " + sku);
    }
}
