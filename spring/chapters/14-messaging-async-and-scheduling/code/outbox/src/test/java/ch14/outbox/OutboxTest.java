package ch14.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Poller switched off: the test calls publishBatch() itself, so every step is deterministic. */
@SpringBootTest(properties = "outbox.poller.enabled=false")
class OutboxTest {

    @Autowired OrderService orders;
    @Autowired OutboxPoller poller;
    @Autowired InMemoryBroker broker;
    @Autowired InventoryConsumer inventory;
    @Autowired JdbcClient jdbc;

    @BeforeEach
    void clean() {
        broker.clear();
        jdbc.sql("DELETE FROM outbox").update();
        jdbc.sql("DELETE FROM orders").update();
        jdbc.sql("DELETE FROM processed_messages").update();
        jdbc.sql("DELETE FROM stock_reservations").update();
    }

    @Test
    void orderAndOutboxRowCommitOrRollBackTogether() {
        assertThatThrownBy(() -> orders.placeOrder("lamp", 4500, true)).hasMessage("crash before commit");
        System.out.println("after rollback: orders=" + count("orders") + " outbox=" + count("outbox"));
        assertThat(count("orders")).isZero();
        assertThat(count("outbox")).isZero();

        orders.placeOrder("book", 1999, false);
        System.out.println("after commit:   orders=" + count("orders") + " outbox=" + count("outbox")
                + " unpublished=" + unpublished());
        System.out.println("outbox payload: " + jdbc.sql("SELECT payload FROM outbox").query(String.class).single());
        assertThat(unpublished()).isEqualTo(1);

        int sent = poller.publishBatch();
        System.out.println("poller sent " + sent + " -> broker has " + broker.delivered().size()
                + ", unpublished=" + unpublished());
        assertThat(broker.delivered()).hasSize(1);
        assertThat(unpublished()).isZero();
    }

    @Test
    void brokerOutageDelaysButDoesNotLose() {
        orders.placeOrder("pen", 150, false);
        orders.placeOrder("ink", 350, false);
        broker.setMode(InMemoryBroker.Mode.DOWN);
        System.out.println("broker DOWN -> sent " + poller.publishBatch() + ", unpublished=" + unpublished()
                + ", attempts of first row=" + jdbc.sql("SELECT attempts FROM outbox ORDER BY id FETCH FIRST 1 ROW ONLY").query(Integer.class).single());
        assertThat(unpublished()).isEqualTo(2);

        broker.setMode(InMemoryBroker.Mode.UP);
        System.out.println("broker UP   -> sent " + poller.publishBatch() + ", unpublished=" + unpublished());
        System.out.println("broker order: " + broker.delivered().stream().map(Message::key).toList());
        assertThat(unpublished()).isZero();
        assertThat(broker.delivered()).hasSize(2);
        assertThat(broker.delivered().get(0).payload()).contains("\"pen\"");   // order kept
    }

    @Test
    void lostAckMeansDuplicateAndTheConsumerMustBeIdempotent() {
        orders.placeOrder("mug", 800, false);
        broker.setMode(InMemoryBroker.Mode.ACK_LOST);
        poller.publishBatch();                              // delivered, but not marked as published
        broker.setMode(InMemoryBroker.Mode.UP);
        poller.publishBatch();                              // sent AGAIN: at-least-once
        System.out.println("broker holds " + broker.delivered().size() + " copies of message "
                + broker.delivered().getFirst().messageId().toString().substring(0, 8) + "...");

        var results = broker.delivered().stream().map(inventory::handle).toList();
        System.out.println("consumer handled: " + results + " -> stock_reservations=" + count("stock_reservations"));
        assertThat(broker.delivered()).hasSize(2);
        assertThat(results).containsExactly(true, false);
        assertThat(count("stock_reservations")).isEqualTo(1);
    }

    private long count(String table) {
        return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single();
    }

    private long unpublished() {
        return jdbc.sql("SELECT count(*) FROM outbox WHERE published_at IS NULL").query(Long.class).single();
    }
}
