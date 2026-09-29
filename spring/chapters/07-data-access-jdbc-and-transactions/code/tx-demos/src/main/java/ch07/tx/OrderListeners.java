package ch07.tx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderListeners {

    public final List<String> calls = new CopyOnWriteArrayList<>();

    @EventListener // runs immediately, inside the publisher's transaction, even if it later rolls back
    void immediately(OrderPlaced e) {
        calls.add("@EventListener " + e.item());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void beforeCommit(OrderPlaced e) {
        calls.add("BEFORE_COMMIT " + e.item());
    }

    @TransactionalEventListener // default phase = AFTER_COMMIT: e.g. send the confirmation e-mail
    void afterCommit(OrderPlaced e) {
        calls.add("AFTER_COMMIT " + e.item());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    void afterRollback(OrderPlaced e) {
        calls.add("AFTER_ROLLBACK " + e.item());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    void afterCompletion(OrderPlaced e) {
        calls.add("AFTER_COMPLETION " + e.item());
    }
}
