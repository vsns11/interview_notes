package ch14.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderListeners {
    private final Received received;

    public OrderListeners(Received received) { this.received = received; }

    /** Group "shipping", 3 consumer threads: the 3 partitions are shared out between them. */
    @KafkaListener(id = "shipping", topics = "orders", groupId = "shipping", concurrency = "3")
    void ship(ConsumerRecord<String, OrderEvent> record) {
        received.add("shipping", record.value().orderId() + " key=" + record.key()
                + " p" + record.partition() + " on " + Thread.currentThread().getName());
    }

    /** Group "billing", 1 thread: a different group gets its OWN copy of every message. */
    @KafkaListener(id = "billing", topics = "orders", groupId = "billing")
    void bill(@Payload OrderEvent event,
              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
              @Header(KafkaHeaders.OFFSET) long offset) {
        received.add("billing", event.orderId() + " p" + partition + "@" + offset
                + " on " + Thread.currentThread().getName());
    }
}
