package ch14.kafka;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PaymentListeners {
    private final Received received;
    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public PaymentListeners(Received received) { this.received = received; }

    @KafkaListener(id = "payments", topics = "payments", groupId = "payments")
    void charge(OrderEvent e) {
        int attempt = attempts.computeIfAbsent(e.orderId(), k -> new AtomicInteger()).incrementAndGet();
        received.add("payments", e.orderId() + " attempt " + attempt);
        if (e.cents() < 0) {
            throw new IllegalArgumentException("negative amount " + e.cents());
        }
        if (e.orderId().equals("p-down") || (e.orderId().equals("p-flaky") && attempt < 3)) {
            throw new TransientPaymentException("provider timeout");
        }
        received.add("payments", e.orderId() + " CHARGED");
    }

    /** Reads the dead-letter topic as plain text (overrides the JSON deserializer for this listener only). */
    @KafkaListener(id = "payments-dlt", topics = "payments-dlt", groupId = "dlt-inspector",
            properties = "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer")
    void inspectDeadLetter(String value,
                           @Header(KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN) String causeClass,
                           @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        received.add("dlt", value + " <- " + originalTopic + ", cause "
                + causeClass.substring(causeClass.lastIndexOf('.') + 1));
    }
}
