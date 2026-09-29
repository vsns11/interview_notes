package com.example.ioc.events;

import com.example.ioc.Trace;
import com.example.ioc.events.OrderEvents.OrderPlaced;
import com.example.ioc.events.OrderEvents.StockReserved;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderListeners {

    @EventListener @Order(1)
    void sendEmail(OrderPlaced e) {
        Trace.add("  email listener for " + e.orderId() + " on " + Thread.currentThread().getName());
    }

    @EventListener @Order(2)
    StockReserved reserveStock(OrderPlaced e) {           // a non-void return value is published as a new event
        Trace.add("  stock listener for " + e.orderId());
        return new StockReserved(e.orderId());
    }

    @EventListener(condition = "#event.cents > 10000")    // SpEL filter: only big orders
    void alertBigOrder(OrderPlaced event) {
        Trace.add("  BIG order alert for " + event.orderId());
    }

    @EventListener
    void onStock(StockReserved e) {
        Trace.add("  shipping listener sees StockReserved(" + e.orderId() + ")");
    }
}
