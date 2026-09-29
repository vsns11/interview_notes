package ch13.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class SchedulersTest {

    static String thread() { return Thread.currentThread().getName().replaceAll("-\\d+$", "-N"); }

    @Test
    void publishOnSwitchesDownstreamSubscribeOnMovesTheSource() {
        List<String> log = new CopyOnWriteArrayList<>();
        Flux.just(1)
            .doOnNext(i -> log.add("source    on " + thread()))
            .publishOn(Schedulers.parallel())                // everything BELOW runs on parallel
            .doOnNext(i -> log.add("after pub on " + thread()))
            .subscribeOn(Schedulers.boundedElastic())        // the subscription (and source) runs here
            .blockLast();
        log.forEach(System.out::println);
        assertThat(log).containsExactly("source    on boundedElastic-N", "after pub on parallel-N");
    }

    @Test
    void wrapABlockingCall() {
        // A blocking API (JDBC, legacy SDK) must not run on the few event-loop/parallel threads.
        Mono<String> legacy = Mono.fromCallable(() -> {
                    Thread.sleep(50);                        // pretend: blocking I/O
                    return "legacy result on " + thread();
                })
                .subscribeOn(Schedulers.boundedElastic());   // dedicated, growable pool for blocking work
        String r = legacy.block();
        System.out.println(r);
        assertThat(r).endsWith("boundedElastic-N");
    }
}
