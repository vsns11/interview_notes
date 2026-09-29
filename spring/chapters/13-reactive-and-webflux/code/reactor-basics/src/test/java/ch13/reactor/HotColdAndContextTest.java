package ch13.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

class HotColdAndContextTest {

    @Test
    void hotSinkLateSubscriberMissesEarlierItems() {
        Sinks.Many<String> prices = Sinks.many().multicast().directBestEffort();
        List<String> early = new CopyOnWriteArrayList<>(), late = new CopyOnWriteArrayList<>();

        prices.asFlux().subscribe(early::add);
        prices.tryEmitNext("p1");
        prices.asFlux().subscribe(late::add);   // joins after p1 was emitted
        prices.tryEmitNext("p2");
        prices.tryEmitComplete();

        System.out.println("early subscriber: " + early + ", late subscriber: " + late);
        assertThat(late).containsExactly("p2");
    }

    @Test
    void contextTravelsUpstream() {
        // Context = per-subscription key/value map (the reactive replacement for ThreadLocal)
        Mono<String> greeting = Mono.deferContextual(ctx -> Mono.just("request " + ctx.get("requestId")));
        StepVerifier.create(greeting.contextWrite(c -> c.put("requestId", "r-42")))
                .expectNext("request r-42").verifyComplete();
        System.out.println("context value read inside the pipeline: r-42");
    }
}
