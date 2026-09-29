package ch14.async;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderListeners {
    private final JdbcClient jdbc;
    private final Trace trace;

    public OrderListeners(JdbcClient jdbc, Trace trace) {
        this.jdbc = jdbc;
        this.trace = trace;
    }

    private long ordersVisible(String item) {
        return jdbc.sql("SELECT count(*) FROM orders WHERE item = ?").param(item).query(Long.class).single();
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void beforeCommit(OrderPlaced e) {
        trace.add("BEFORE_COMMIT    " + e.item() + " (row visible in tx: " + ordersVisible(e.item()) + ")");
    }

    @TransactionalEventListener   // phase = AFTER_COMMIT is the default
    void afterCommit(OrderPlaced e) {
        trace.add("AFTER_COMMIT     " + e.item() + " on " + Trace.thread());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    void afterRollback(OrderPlaced e) {
        trace.add("AFTER_ROLLBACK   " + e.item() + " (rows now: " + ordersVisible(e.item()) + ")");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    void afterCompletion(OrderPlaced e) {
        trace.add("AFTER_COMPLETION " + e.item());
    }

    /** The common production combo: only after commit, off the request thread, in its own transaction. */
    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void emailAfterCommit(OrderPlaced e) {
        jdbc.sql("INSERT INTO email_log(message) VALUES (?)").param("confirmation for " + e.item()).update();
        trace.add("async AFTER_COMMIT email for " + e.item() + " on " + Trace.thread());
    }
}
