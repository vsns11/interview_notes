package ch14.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

class ErrorHandlingTest extends KafkaTestBase {

    @Autowired OrderProducer producer;
    @Autowired Received received;
    @Autowired EmbeddedKafkaBroker broker;

    @Test
    void blockingRetriesThenDeadLetterTopic() {
        producer.pay(new OrderEvent("p-ok", "alice", 100)).join();
        producer.pay(new OrderEvent("p-flaky", "bob", 200)).join();
        producer.pay(new OrderEvent("p-down", "carol", 300)).join();
        producer.pay(new OrderEvent("p-bad", "dave", -5)).join();
        sendRawText("payments", "p-junk", "this is not JSON");

        await().atMost(Duration.ofSeconds(30)).until(() -> received.of("dlt").size() == 3);
        System.out.println("--- payments listener ---");
        received.of("payments").forEach(System.out::println);
        System.out.println("--- payments-dlt ---");
        received.of("dlt").forEach(System.out::println);

        assertThat(received.of("payments")).containsExactly(
                "p-ok attempt 1", "p-ok CHARGED",
                "p-flaky attempt 1", "p-flaky attempt 2", "p-flaky attempt 3", "p-flaky CHARGED",
                "p-down attempt 1", "p-down attempt 2", "p-down attempt 3",
                "p-bad attempt 1");                          // not retryable: straight to the DLT
        assertThat(received.of("dlt")).hasSize(3);
        assertThat(received.of("dlt").get(2)).startsWith("this is not JSON <- payments");
    }

    @Test
    void nonBlockingRetryTopics() {
        producer.invoice(new OrderEvent("inv-bad", "erin", 700)).join();
        producer.invoice(new OrderEvent("inv-ok", "frank", 800)).join();

        await().atMost(Duration.ofSeconds(30))
                .until(() -> received.of("invoices").stream().anyMatch(l -> l.contains("DLT handler")));
        System.out.println("--- @RetryableTopic ---");
        received.of("invoices").forEach(System.out::println);
        assertThat(received.of("invoices")).containsSubsequence(
                "inv-bad from invoices", "inv-bad from invoices-retry", "inv-bad from invoices-retry",
                "inv-bad DLT handler, from invoices-dlt");
        assertThat(received.of("invoices").indexOf("inv-ok from invoices"))   // not stuck behind the failing record
                .isLessThan(received.of("invoices").indexOf("inv-bad from invoices-retry"));
    }

    private void sendRawText(String topic, String key, String text) {
        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var pf = new DefaultKafkaProducerFactory<String, String>(props);
        new KafkaTemplate<>(pf).send(topic, key, text).join();
        pf.destroy();
    }
}
