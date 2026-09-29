package ch13.reactor;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OperatorsTest {

    /** Simulated remote call: user N answers after (4 - N) seconds, so later users answer first. */
    static Flux<String> ordersOf(int user) {
        return Flux.just("u" + user + "-a", "u" + user + "-b")
                .delayElements(Duration.ofSeconds(4 - user));
    }

    @Test
    void mapFilterReduce() {
        StepVerifier.create(Flux.range(1, 10).filter(i -> i % 2 == 0).map(i -> i * i).reduce(0, Integer::sum))
                .expectNext(220)   // 4 + 16 + 36 + 64 + 100
                .verifyComplete();
    }

    @Test
    void flatMapInterleavesConcatMapKeepsOrder() {
        // withVirtualTime: the delays run on a fake clock, so this 12-"second" test takes milliseconds
        StepVerifier.withVirtualTime(() -> Flux.range(1, 3).flatMap(OperatorsTest::ordersOf).collectList())
                .thenAwait(Duration.ofHours(1))
                .consumeNextWith(l -> System.out.println("flatMap           : " + l))
                .verifyComplete();

        StepVerifier.withVirtualTime(() -> Flux.range(1, 3).concatMap(OperatorsTest::ordersOf).collectList())
                .thenAwait(Duration.ofHours(1))
                .consumeNextWith(l -> System.out.println("concatMap         : " + l))
                .verifyComplete();

        StepVerifier.withVirtualTime(() -> Flux.range(1, 3).flatMapSequential(OperatorsTest::ordersOf).collectList())
                .thenAwait(Duration.ofHours(1))
                .consumeNextWith(l -> System.out.println("flatMapSequential : " + l))
                .verifyComplete();
    }

    @Test
    void howLongDoTheyTake() {
        // concatMap: user 1 (3 s + 3 s), then user 2 (2 s + 2 s), then user 3 (1 s + 1 s) = 12 s
        var concat = StepVerifier.withVirtualTime(() -> Flux.range(1, 3).concatMap(OperatorsTest::ordersOf))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(11)).expectNextCount(5)
                .expectNoEvent(Duration.ofMillis(999))
                .thenAwait(Duration.ofMillis(1)).expectNext("u3-b").verifyComplete();
        // flatMap: all three run at the same time, the slowest (user 1) needs 6 s
        var flat = StepVerifier.withVirtualTime(() -> Flux.range(1, 3).flatMap(OperatorsTest::ordersOf))
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(5)).expectNextCount(5)
                .thenAwait(Duration.ofSeconds(1)).expectNext("u1-b").verifyComplete();
        System.out.println("concatMap needs 12 virtual s, flatMap 6 virtual s; real time: "
                + concat.toMillis() + " ms and " + flat.toMillis() + " ms");
    }

    @Test
    void combining() {
        Flux<String> names = Flux.just("Ada", "Linus", "Grace");
        Flux<Integer> years = Flux.just(1815, 1969, 1906);
        StepVerifier.create(Flux.zip(names, years, (n, y) -> n + "@" + y))
                .expectNext("Ada@1815", "Linus@1969", "Grace@1906").verifyComplete();

        StepVerifier.create(Flux.concat(Flux.just(1, 2), Flux.just(3)))        // one after the other
                .expectNext(1, 2, 3).verifyComplete();

        Mono<String> fromCache = Mono.empty();
        StepVerifier.create(fromCache.switchIfEmpty(Mono.just("from DB")))      // fallback when empty
                .expectNext("from DB").verifyComplete();

        StepVerifier.create(Flux.range(1, 7).buffer(3))
                .expectNext(java.util.List.of(1, 2, 3), java.util.List.of(4, 5, 6), java.util.List.of(7))
                .verifyComplete();
        System.out.println("zip, concat, switchIfEmpty, buffer verified");
    }
}
