package ch16.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RetryAndRateLimiterTests {

    @Autowired StockService service;
    @Autowired CoreRetryStockService coreService;
    @Autowired RemoteInventory remote;
    @Autowired RetryRegistry retryRegistry;

    @Test
    void resilience4jRetry() {
        remote.reset();
        retryRegistry.retry("inventory-retry").getEventPublisher()
                .onRetry(e -> System.out.println("   retry #" + e.getNumberOfRetryAttempts()
                        + " after " + e.getLastThrowable().getMessage() + ", waiting " + e.getWaitInterval().toMillis() + " ms"));
        remote.failNextCalls(2);
        System.out.println("Resilience4j @Retry, remote fails twice:");
        String result = service.stockWithRetry("A");
        System.out.println("   -> " + result + " after " + remote.calls() + " attempts");
        assertThat(remote.calls()).isEqualTo(3);

        remote.reset();
        remote.setDown(true);
        assertThatThrownBy(() -> service.stockWithRetry("A")).isInstanceOf(RemoteCallException.class);
        System.out.println("   remote down: gave up after " + remote.calls() + " attempts, caller gets RemoteCallException");
    }

    @Test
    void frameworkRetryableDoesTheSame() {
        remote.reset();
        remote.failNextCalls(2);
        String result = coreService.stock("A");
        System.out.println("Framework 7 @Retryable, remote fails twice -> " + result + " after " + remote.calls() + " attempts");
        assertThat(remote.calls()).isEqualTo(3);
    }

    @Test
    void rateLimiterRejectsTheFourthCall() {
        StringBuilder log = new StringBuilder("RateLimiter (3 per 10 s): ");
        for (int i = 1; i <= 5; i++) {
            try {
                service.search("q" + i);
                log.append("call").append(i).append("=OK ");
            } catch (RequestNotPermitted e) {
                log.append("call").append(i).append("=REJECTED ");
            }
        }
        System.out.println(log.toString().trim());
        assertThat(log.toString()).contains("call3=OK", "call4=REJECTED", "call5=REJECTED");
    }
}
