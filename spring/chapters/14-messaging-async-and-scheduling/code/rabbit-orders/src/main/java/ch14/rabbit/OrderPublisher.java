package ch14.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisher {
    private final RabbitTemplate rabbit;

    public OrderPublisher(RabbitTemplate rabbit) { this.rabbit = rabbit; }

    public void placed(OrderEvent e, String region) {
        // exchange, routing key, body (converted to JSON)
        rabbit.convertAndSend("orders", "order.placed." + region, e);
    }
}
