package ch14.outbox;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
public class OrderService {
    /** The event body, serialized to JSON into the outbox row. */
    public record OrderPlaced(long orderId, String item, long cents) {}

    private final JdbcClient jdbc;
    private final JsonMapper json;

    public OrderService(JdbcClient jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    /** ONE local transaction: the order row and the outbox row commit together or not at all. */
    @Transactional
    public long placeOrder(String item, long cents, boolean failBeforeCommit) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO orders(item, cents) VALUES (?, ?)").params(item, cents).update(keys, "id");
        long orderId = keys.getKey().longValue();

        String payload = json.writeValueAsString(new OrderPlaced(orderId, item, cents));
        jdbc.sql("""
                INSERT INTO outbox(message_id, aggregate_id, event_type, payload)
                VALUES (?, ?, 'OrderPlaced', ?)""")
            .params(UUID.randomUUID(), "order-" + orderId, payload)
            .update();

        if (failBeforeCommit) {
            throw new IllegalStateException("crash before commit");
        }
        return orderId;       // no broker call here: nothing can be half-done
    }
}
