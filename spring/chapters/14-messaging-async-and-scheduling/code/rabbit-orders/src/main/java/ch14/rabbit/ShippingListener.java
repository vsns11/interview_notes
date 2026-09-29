package ch14.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShippingListener {

    /** Competing consumers: every instance listening on the same QUEUE shares the messages. */
    @RabbitListener(queues = "orders.shipping")
    void ship(OrderEvent e) {
        if (e.cents() < 0) {
            throw new IllegalArgumentException("bad amount");   // 1 attempt + 2 retries, then dead-lettered
        }
        System.out.println("shipping " + e.orderId());
    }

    @RabbitListener(queues = "orders.shipping.dlq")
    void inspect(OrderEvent e) {
        System.out.println("dead letter: " + e.orderId());
    }
}
