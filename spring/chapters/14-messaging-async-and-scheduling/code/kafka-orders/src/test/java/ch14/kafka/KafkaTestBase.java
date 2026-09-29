package ch14.kafka;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

/**
 * One in-JVM Kafka broker (KRaft mode, random port) for all test classes: identical annotations
 * mean Spring's test-context cache reuses the same context and broker.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1"})
abstract class KafkaTestBase {
}
