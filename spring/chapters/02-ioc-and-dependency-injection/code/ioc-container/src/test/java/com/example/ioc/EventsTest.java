package com.example.ioc;

import com.example.ioc.events.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class EventsTest {

    @Test
    void listenersRunSynchronouslyInOrder() {
        Trace.clear();
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.events")) {
            var orders = ctx.getBean(OrderService.class);
            orders.place("A-1", 2_500);
            orders.place("B-2", 50_000);
        }
        Trace.print("events");
        assertThat(Trace.lines()).containsSubsequence(
                "OrderService: saving A-1 on main", "  email listener for A-1 on main", "  stock listener for A-1",
                "  shipping listener sees StockReserved(A-1)", "OrderService: publishEvent returned");
        assertThat(Trace.lines()).filteredOn(l -> l.contains("BIG")).containsExactly("  BIG order alert for B-2");
    }
}
