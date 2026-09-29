package ch14.kafka;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Non-blocking retries: a failed record is re-published to invoices-retry and the main topic
 * keeps flowing. After the last attempt it goes to invoices-dlt and the @DltHandler runs.
 */
@Component
public class InvoiceListener {
    private final Received received;

    public InvoiceListener(Received received) { this.received = received; }

    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 200))
    @KafkaListener(id = "invoices", topics = "invoices", groupId = "invoices")
    void createInvoice(OrderEvent e, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        received.add("invoices", e.orderId() + " from " + topic);
        if (e.orderId().startsWith("inv-bad")) {
            throw new IllegalStateException("tax service unavailable");
        }
    }

    @DltHandler
    void giveUp(OrderEvent e, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        received.add("invoices", e.orderId() + " DLT handler, from " + topic);
    }
}
