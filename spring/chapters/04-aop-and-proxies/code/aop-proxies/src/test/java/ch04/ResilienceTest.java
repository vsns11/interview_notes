package ch04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch04.resilience.InventoryClient;
import ch04.resilience.ReportPrinter;
import ch04.resilience.TransientException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.resilience.InvocationRejectedException;

@SpringBootTest
class ResilienceTest {

    @Autowired InventoryClient inventory;
    @Autowired ReportPrinter printer;

    @Test
    void retryableRecoversFromTwoFailures() {
        inventory.failNextCalls(2);
        int stock = inventory.stockFor("SKU-1");
        System.out.println("stock = " + stock + " after " + inventory.attempts());
        assertThat(stock).isEqualTo(42);
        assertThat(inventory.attempts()).hasSize(3);
    }

    @Test
    void retryableGivesUpAfterMaxRetries() {
        inventory.failNextCalls(10);
        assertThatThrownBy(() -> inventory.stockFor("SKU-1"))
                .isInstanceOf(TransientException.class);       // the LAST original exception
        System.out.println("gave up after " + inventory.attempts());
        assertThat(inventory.attempts()).hasSize(4);           // 1 call + 3 retries
    }

    @Test
    void nonMatchingExceptionIsNotRetried() {
        inventory.failNextCalls(0);
        assertThatThrownBy(() -> inventory.stockForBadSku("??"))
                .isInstanceOf(IllegalArgumentException.class);
        System.out.println("IllegalArgumentException -> " + inventory.attempts());
        assertThat(inventory.attempts()).hasSize(1);
    }

    @Test
    void concurrencyLimitCapsParallelCallers() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> calls = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                int n = i;
                calls.add(pool.submit(() -> { printer.print("doc-" + n); return null; }));
            }
            for (Future<?> f : calls) f.get();
        }
        System.out.println("6 callers, max inside print() at once = " + printer.maxConcurrent());
        assertThat(printer.maxConcurrent()).isEqualTo(2);
    }

    @Test
    void rejectPolicyFailsFast() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = pool.submit(() -> { printer.printUrgent("a"); return null; });
            Thread.sleep(30);                              // let the first caller get in
            assertThatThrownBy(() -> printer.printUrgent("b"))
                    .isInstanceOf(InvocationRejectedException.class)
                    .satisfies(e -> System.out.println("second caller -> " + e.getClass().getSimpleName()));
            first.get();
        }
    }

    @Test
    void programmaticRetryTemplate() throws RetryException {
        AtomicInteger calls = new AtomicInteger();
        RetryPolicy policy = RetryPolicy.builder()
                .maxRetries(4)
                .delay(Duration.ofMillis(5))
                .multiplier(2)
                .includes(TransientException.class)
                .build();
        RetryTemplate retry = new RetryTemplate(policy);

        String result = retry.execute(() -> {
            if (calls.incrementAndGet() < 3) throw new TransientException("busy");
            return "done on call " + calls.get();
        });
        System.out.println("RetryTemplate -> " + result);
        assertThat(result).isEqualTo("done on call 3");
    }
}
