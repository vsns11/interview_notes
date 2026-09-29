package ch16.resilience;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CircuitBreakerTests {

    @Autowired StockService service;
    @Autowired RemoteInventory remote;
    @Autowired CircuitBreakerRegistry registry;

    @Test
    void closedToOpenToHalfOpenToClosed() throws InterruptedException {
        remote.reset();
        CircuitBreaker breaker = registry.circuitBreaker("inventory");
        List<String> transitions = new ArrayList<>();
        breaker.getEventPublisher().onStateTransition(e ->
                transitions.add(e.getStateTransition().getFromState() + " -> " + e.getStateTransition().getToState()));

        System.out.println("1) dependency goes down, 4 calls:");
        remote.setDown(true);
        for (int i = 1; i <= 4; i++) {
            System.out.println("   call " + i + ": " + service.stockWithBreaker("A") + "  state=" + breaker.getState());
        }
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        System.out.println("2) while OPEN the remote is not even called:");
        int before = remote.calls();
        System.out.println("   call 5: " + service.stockWithBreaker("A") + "  remote calls +" + (remote.calls() - before));
        assertThat(remote.calls()).isEqualTo(before);

        System.out.println("3) wait 300 ms, dependency is back; 2 trial calls in HALF_OPEN:");
        Thread.sleep(350);
        remote.setDown(false);
        System.out.println("   call 6: " + service.stockWithBreaker("A") + "  state=" + breaker.getState());
        System.out.println("   call 7: " + service.stockWithBreaker("A") + "  state=" + breaker.getState());

        System.out.println("transitions: " + transitions);
        assertThat(transitions).containsExactly("CLOSED -> OPEN", "OPEN -> HALF_OPEN", "HALF_OPEN -> CLOSED");
    }
}
