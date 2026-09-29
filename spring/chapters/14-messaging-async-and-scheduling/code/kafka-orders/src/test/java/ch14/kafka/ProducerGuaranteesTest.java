package ch14.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

class ProducerGuaranteesTest extends KafkaTestBase {

    @Autowired EmbeddedKafkaBroker broker;
    @Autowired ProducerFactory<String, Object> bootProducerFactory;
    @Autowired OrderProducer producer;

    @Test
    void idempotenceIsOnByDefault() {
        // ProducerConfig resolves defaults exactly like a real KafkaProducer would
        var effective = new ProducerConfig(bootProducerFactory.getConfigurationProperties());
        System.out.println("enable.idempotence = " + effective.getBoolean(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        System.out.println("acks               = " + effective.getString(ProducerConfig.ACKS_CONFIG));
        System.out.println("max.in.flight      = " + effective.getInt(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
        assertThat(effective.getBoolean(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isTrue();
        assertThat(effective.getString(ProducerConfig.ACKS_CONFIG)).isEqualTo("-1");   // -1 = all
    }

    @Test
    void jsonOnTheWire() {
        producer.publish(new OrderEvent("o-json", "zoe", 4200)).join();
        try (Consumer<String, String> raw = stringConsumer("wire-inspector", "read_uncommitted")) {
            raw.subscribe(List.of("orders"));
            ConsumerRecord<String, String> rec = null;
            for (ConsumerRecord<String, String> r : KafkaTestUtils.getRecords(raw, Duration.ofSeconds(10))) {
                if (r.value().contains("o-json")) rec = r;
            }
            assertThat(rec).isNotNull();
            String typeHeader = new String(rec.headers().lastHeader("__TypeId__").value());
            System.out.println("value bytes as text: " + rec.value());
            System.out.println("__TypeId__ header  : " + typeHeader);
            assertThat(rec.value()).isEqualTo("{\"orderId\":\"o-json\",\"customer\":\"zoe\",\"cents\":4200}");
            assertThat(typeHeader).isEqualTo("ch14.kafka.OrderEvent");
        }
    }

    @Test
    void transactionalProducerAndReadCommitted() {
        Map<String, Object> props = KafkaTestUtils.producerProps(broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var pf = new DefaultKafkaProducerFactory<String, String>(props);
        pf.setTransactionIdPrefix("ledger-tx-");        // turns on transactions (and requires idempotence)
        var tx = new KafkaTemplate<>(pf);

        tx.executeInTransaction(t -> {                   // committed: both records become visible together
            t.send("ledger", "acc-1", "debit 50");
            t.send("ledger", "acc-2", "credit 50");
            return null;
        });
        assertThatThrownBy(() -> tx.executeInTransaction(t -> {   // aborted: the record stays invisible
            t.send("ledger", "acc-1", "debit 999");
            t.flush();                                   // the record really reaches the broker...
            throw new IllegalStateException("crash before commit");   // ...but the transaction is aborted
        })).hasMessage("crash before commit");

        List<String> committed = readAll("read_committed");
        List<String> everything = readAll("read_uncommitted");
        System.out.println("isolation.level=read_committed   -> " + committed);
        System.out.println("isolation.level=read_uncommitted -> " + everything);
        assertThat(committed).containsExactly("debit 50", "credit 50");
        assertThat(everything).containsExactly("debit 50", "credit 50", "debit 999");
        pf.destroy();
    }

    private List<String> readAll(String isolation) {
        try (Consumer<String, String> c = stringConsumer("ledger-" + isolation, isolation)) {
            c.subscribe(List.of("ledger"));
            List<String> values = new ArrayList<>();
            KafkaTestUtils.getRecords(c, Duration.ofSeconds(5)).forEach(r -> values.add(r.value()));
            return values;
        }
    }

    private Consumer<String, String> stringConsumer(String group, String isolation) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(broker, group, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolation);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer()).createConsumer();
    }
}
