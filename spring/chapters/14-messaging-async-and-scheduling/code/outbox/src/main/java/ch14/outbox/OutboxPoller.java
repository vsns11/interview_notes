package ch14.outbox;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The "message relay": reads unsent outbox rows in id order, publishes them, marks them sent.
 * At-least-once: if the process dies after publish() but before the UPDATE, the row is sent again.
 */
@Component
public class OutboxPoller {
    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final JdbcClient jdbc;
    private final MessagePublisher publisher;

    public OutboxPoller(JdbcClient jdbc, MessagePublisher publisher) {
        this.jdbc = jdbc;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${outbox.poller.delay-ms}")
    public void poll() {
        publishBatch();
    }

    /** Returns how many messages were published in this round. */
    @Transactional
    public int publishBatch() {
        // FOR UPDATE SKIP LOCKED: several app instances can poll at once without sending the same row twice
        List<OutboxRow> rows = jdbc.sql("""
                SELECT id, message_id, aggregate_id, event_type, payload
                FROM outbox
                WHERE published_at IS NULL
                ORDER BY id
                FETCH FIRST 10 ROWS ONLY
                FOR UPDATE SKIP LOCKED""")
            .query(OutboxRow.class).list();

        int sent = 0;
        for (OutboxRow row : rows) {
            try {
                publisher.publish(new Message(row.messageId(), row.aggregateId(), row.eventType(), row.payload()));
            } catch (Exception e) {
                jdbc.sql("UPDATE outbox SET attempts = attempts + 1 WHERE id = ?").param(row.id()).update();
                log.warn("outbox row {} not published ({}), will retry", row.id(), e.getMessage());
                break;                                  // keep order: do not send later rows before this one
            }
            jdbc.sql("UPDATE outbox SET published_at = CURRENT_TIMESTAMP, attempts = attempts + 1 WHERE id = ?")
                .param(row.id()).update();
            sent++;
        }
        return sent;
    }

    record OutboxRow(long id, UUID messageId, String aggregateId, String eventType, String payload) {}
}
