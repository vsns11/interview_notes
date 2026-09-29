package ch14.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {
    private final KafkaTemplate<String, Object> kafka;

    public OrderProducer(KafkaTemplate<String, Object> kafka) { this.kafka = kafka; }

    /** Key = customer: all events of one customer go to the same partition, so they stay in order. */
    public CompletableFuture<SendResult<String, Object>> publish(OrderEvent event) {
        return kafka.send("orders", event.customer(), event);
    }

    public CompletableFuture<SendResult<String, Object>> pay(OrderEvent event) {
        return kafka.send("payments", event.orderId(), event);
    }

    public CompletableFuture<SendResult<String, Object>> invoice(OrderEvent event) {
        return kafka.send("invoices", event.orderId(), event);
    }
}
