package ch13.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CreationAndLazinessTest {

    @Test
    void monoAndFluxBasics() {
        StepVerifier.create(Mono.just("one")).expectNext("one").verifyComplete();
        StepVerifier.create(Mono.empty()).verifyComplete();                      // 0 items, then complete
        StepVerifier.create(Flux.range(1, 3)).expectNext(1, 2, 3).verifyComplete();
        StepVerifier.create(Flux.fromIterable(List.of("a", "b"))).expectNext("a", "b").verifyComplete();
        StepVerifier.create(Flux.error(new IllegalStateException("boom")))
                .expectErrorMessage("boom").verify();
        System.out.println("just/empty/range/fromIterable/error verified");
    }

    @Test
    void nothingHappensUntilYouSubscribe() {
        AtomicInteger calls = new AtomicInteger();
        Flux<Integer> pipeline = Flux.range(1, 3)
                .map(i -> { calls.incrementAndGet(); return i * 10; });

        System.out.println("after assembling the pipeline, map ran " + calls.get() + " times");
        assertThat(calls).hasValue(0);

        List<Integer> out = pipeline.collectList().block();   // block() subscribes (fine in a test)
        System.out.println("after subscribing: " + out + ", map ran " + calls.get() + " times");
        assertThat(calls).hasValue(3);

        pipeline.blockLast();                                 // cold: a 2nd subscriber re-runs everything
        System.out.println("after a second subscriber, map ran " + calls.get() + " times");
        assertThat(calls).hasValue(6);
    }

    @Test
    void justIsEagerDeferIsLazy() {
        AtomicInteger dbHits = new AtomicInteger();
        Mono<Integer> eager = Mono.just(dbHits.incrementAndGet());                 // runs NOW, once
        Mono<Integer> lazy  = Mono.defer(() -> Mono.just(dbHits.incrementAndGet())); // runs per subscribe
        System.out.println("hits after building both monos: " + dbHits.get());
        eager.block(); eager.block(); lazy.block(); lazy.block();
        System.out.println("hits after 2x eager + 2x lazy subscribe: " + dbHits.get());
        assertThat(dbHits).hasValue(3);   // 1 (just) + 2 (defer)
    }
}
