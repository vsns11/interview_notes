package ch14.async;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final JdbcClient jdbc;
    private final ApplicationEventPublisher events;
    private final Trace trace;

    public OrderService(JdbcClient jdbc, ApplicationEventPublisher events, Trace trace) {
        this.jdbc = jdbc;
        this.events = events;
        this.trace = trace;
    }

    @Transactional
    public void place(String item, boolean paymentFails) {
        jdbc.sql("INSERT INTO orders(item) VALUES (?)").param(item).update();
        events.publishEvent(new OrderPlaced(item));
        trace.add("service: event published inside the transaction");
        if (paymentFails) {
            throw new IllegalStateException("payment declined");
        }
    }
}
