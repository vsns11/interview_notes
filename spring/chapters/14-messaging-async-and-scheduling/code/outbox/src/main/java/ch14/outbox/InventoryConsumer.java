package ch14.outbox;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent consumer: remembers message ids, so a redelivered message has no second effect. */
@Component
public class InventoryConsumer {
    private final JdbcClient jdbc;

    public InventoryConsumer(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional
    public boolean handle(Message m) {
        try {
            jdbc.sql("INSERT INTO processed_messages(message_id) VALUES (?)").param(m.messageId()).update();
        } catch (DuplicateKeyException alreadySeen) {
            return false;                               // duplicate: skip silently
        }
        jdbc.sql("INSERT INTO stock_reservations(order_id) VALUES (?)").param(m.key()).update();
        return true;
    }
}
