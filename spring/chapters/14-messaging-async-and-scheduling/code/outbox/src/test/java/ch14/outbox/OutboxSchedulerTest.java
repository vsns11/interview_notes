package ch14.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Poller switched on (every 200 ms): the order shows up at the broker by itself. */
@SpringBootTest
class OutboxSchedulerTest {

    @Autowired OrderService orders;
    @Autowired InMemoryBroker broker;

    @Test
    void pollerPublishesInTheBackground() {
        long start = System.currentTimeMillis();
        orders.placeOrder("desk", 25_000, false);
        await().atMost(Duration.ofSeconds(3)).until(() -> broker.delivered().size() == 1);
        System.out.println("published by the scheduled poller within ~" + ((System.currentTimeMillis() - start) / 100 * 100 + 100)
                + " ms: " + broker.delivered().getFirst().type() + " " + broker.delivered().getFirst().payload());
        assertThat(broker.delivered().getFirst().key()).startsWith("order-");
    }
}
