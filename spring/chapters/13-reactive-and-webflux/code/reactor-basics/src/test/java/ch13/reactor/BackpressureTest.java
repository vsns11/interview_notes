package ch13.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

class BackpressureTest {

    @Test
    void subscriberControlsTheRate() {
        List<Long> requests = new CopyOnWriteArrayList<>();
        List<Integer> received = new ArrayList<>();

        Flux.range(1, 10)
            .doOnRequest(requests::add)                  // what the subscriber asked for
            .subscribe(new BaseSubscriber<Integer>() {
                @Override protected void hookOnSubscribe(Subscription s) { request(2); }  // start with 2
                @Override protected void hookOnNext(Integer v) {
                    received.add(v);
                    if (received.size() % 2 == 0) request(2);                            // then 2 at a time
                }
            });

        System.out.println("requests = " + requests + "\nreceived = " + received);
        assertThat(received).hasSize(10);
        assertThat(requests).allMatch(n -> n == 2);
    }

    @Test
    void limitRateReplenishesInBatches() {
        List<Long> upstreamRequests = new CopyOnWriteArrayList<>();
        Flux.range(1, 100)
            .doOnRequest(upstreamRequests::add)
            .limitRate(10)                                // never ask upstream for more than 10
            .blockLast();                                 // downstream asks for "unbounded"
        System.out.println("limitRate(10) upstream requests = " + upstreamRequests);
        assertThat(upstreamRequests.getFirst()).isEqualTo(10L);
        assertThat(upstreamRequests).allMatch(n -> n <= 10);
    }

    @Test
    void stepVerifierCanRequestManually() {
        StepVerifier.create(Flux.range(1, 5), 0)          // initial demand 0
                .expectSubscription()
                .expectNoEvent(java.time.Duration.ofMillis(50))   // nothing arrives without demand
                .thenRequest(2).expectNext(1, 2)
                .thenRequest(3).expectNext(3, 4, 5)
                .verifyComplete();
        System.out.println("demand 0 -> nothing; request(2) -> 1,2; request(3) -> 3,4,5");
    }

    /** A push source that ignores demand, like a sensor or a price ticker: emits 1..20 immediately. */
    static Flux<Integer> ticker() {
        return Flux.create(sink -> {
            for (int i = 1; i <= 20; i++) sink.next(i);
            sink.complete();
        }, FluxSink.OverflowStrategy.IGNORE);   // IGNORE = no built-in protection, the operator below decides
    }

    @Test
    void overflowStrategies() {
        // The consumer only asks for 5 items; the ticker pushes 20.
        List<Integer> dropped = new ArrayList<>();
        StepVerifier.create(ticker().onBackpressureDrop(dropped::add), 5)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
        System.out.println("DROP   : kept 1..5, dropped " + dropped.size() + " items (" + dropped.getFirst() + ".." + dropped.getLast() + ")");

        // A slow consumer: asks for 5 now, and for more only after the ticker has pushed everything.
        SlowConsumer latest = new SlowConsumer();
        ticker().onBackpressureLatest().subscribe(latest);   // 20 items pushed during this call
        latest.request(5);
        System.out.println("LATEST : " + latest.got + " " + latest.end);
        assertThat(latest.got).containsExactly(1, 2, 3, 4, 5, 20);   // only the newest waiting item survived

        SlowConsumer buffered = new SlowConsumer();
        ticker().onBackpressureBuffer(10).subscribe(buffered);  // 6..15 fit in the buffer, 16 overflows
        buffered.request(50);
        System.out.println("BUFFER(10): " + buffered.got + " " + buffered.end);
        assertThat(buffered.got).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        assertThat(buffered.end).startsWith("error: The receiver is overrun");
    }

    /** Requests 5 items on subscribe and records what arrives. */
    static class SlowConsumer extends BaseSubscriber<Integer> {
        final List<Integer> got = new ArrayList<>();
        String end = "(still open)";
        @Override protected void hookOnSubscribe(Subscription s) { request(5); }
        @Override protected void hookOnNext(Integer v) { got.add(v); }
        @Override protected void hookOnComplete() { end = "complete"; }
        @Override protected void hookOnError(Throwable e) { end = "error: " + e.getMessage(); }
    }
}
