package ch13.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.retry.Retry;

class ErrorHandlingTest {

    @Test
    void anErrorEndsTheStream() {
        StepVerifier.create(Flux.just(1, 2, 0, 4).map(i -> 12 / i))
                .expectNext(12, 6)
                .expectError(ArithmeticException.class)   // 4 is never processed
                .verify();
    }

    @Test
    void fallbacks() {
        Flux<Integer> risky = Flux.just(1, 2, 0, 4).map(i -> 12 / i);
        StepVerifier.create(risky.onErrorReturn(-1)).expectNext(12, 6, -1).verifyComplete();
        StepVerifier.create(risky.onErrorResume(e -> Flux.just(100, 200))).expectNext(12, 6, 100, 200).verifyComplete();
        StepVerifier.create(risky.onErrorMap(e -> new IllegalArgumentException("bad input", e)))
                .expectNext(12, 6).expectErrorMessage("bad input").verify();
        System.out.println("onErrorReturn / onErrorResume / onErrorMap verified");
    }

    @Test
    void retryResubscribes() {
        AtomicInteger attempts = new AtomicInteger();
        Mono<String> flaky = Mono.fromCallable(() -> {
            if (attempts.incrementAndGet() < 3) throw new IllegalStateException("try again");
            return "ok on attempt " + attempts.get();
        });
        StepVerifier.create(flaky.retry(5)).expectNext("ok on attempt 3").verifyComplete();
        System.out.println("retry(5): succeeded after " + attempts.get() + " attempts");
    }

    @Test
    void retryWithExponentialBackoffOnVirtualTime() {
        List<Long> attemptTimes = new ArrayList<>();
        StepVerifier.withVirtualTime(() -> {
                    VirtualTimeScheduler clock = VirtualTimeScheduler.get();
                    return Mono.defer(() -> {
                                attemptTimes.add(clock.now(java.util.concurrent.TimeUnit.MILLISECONDS));
                                return Mono.<String>error(new IllegalStateException("down"));
                            })
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).jitter(0)); // 1 s, 2 s, 4 s
                })
                .thenAwait(Duration.ofMinutes(1))
                .expectErrorMatches(e -> e.getMessage().startsWith("Retries exhausted: 3/3"))
                .verify();
        System.out.println("attempts at virtual ms: " + attemptTimes);
        assertThat(attemptTimes).containsExactly(0L, 1000L, 3000L, 7000L);
    }

    @Test
    void timeoutWithFallback() {
        StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofSeconds(5)).map(x -> "slow answer")
                        .timeout(Duration.ofSeconds(2), Mono.just("fallback")))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(2))
                .expectNext("fallback")
                .verifyComplete();

        StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofSeconds(5)).timeout(Duration.ofSeconds(2)))
                .thenAwait(Duration.ofSeconds(2))
                .expectError(TimeoutException.class)
                .verify();
        System.out.println("timeout(2s) on a 5 s call -> fallback / TimeoutException");
    }
}
