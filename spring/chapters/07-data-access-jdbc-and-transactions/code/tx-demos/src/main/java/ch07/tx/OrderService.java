package ch07.tx;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final Db db;
    private final ApplicationEventPublisher events;

    public OrderService(Db db, ApplicationEventPublisher events) {
        this.db = db;
        this.events = events;
    }

    @Transactional
    public void place(String item, boolean failAfterPublish) {
        db.audit("order " + item);
        events.publishEvent(new OrderPlaced(item));
        if (failAfterPublish) {
            throw new IllegalStateException("payment declined");
        }
    }

    /** No transaction here: @TransactionalEventListener methods are skipped (unless fallbackExecution = true). */
    public void placeWithoutTransaction(String item) {
        events.publishEvent(new OrderPlaced(item));
    }
}
