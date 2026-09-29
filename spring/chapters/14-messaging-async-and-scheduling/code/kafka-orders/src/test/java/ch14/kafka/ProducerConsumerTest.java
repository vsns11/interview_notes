package ch14.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

class ProducerConsumerTest extends KafkaTestBase {

    @Autowired OrderProducer producer;
    @Autowired Received received;
    @Autowired KafkaListenerEndpointRegistry registry;

    @Test
    void keysPartitionsAndConsumerGroups() {
        // wait until the group has settled: each of the 3 'shipping' consumers owns exactly one partition
        var shipping = (ConcurrentMessageListenerContainer<?, ?>) registry.getListenerContainer("shipping");
        await().atMost(Duration.ofSeconds(30)).until(() -> shipping.getContainers().stream()
                .allMatch(c -> c.getAssignedPartitions() != null && c.getAssignedPartitions().size() == 1));
        shipping.getContainers().forEach(c ->
                System.out.println("assignment: " + c.getListenerId() + " -> " + c.getAssignedPartitions()));

        List<OrderEvent> events = List.of(
                new OrderEvent("o-1", "alice", 1200), new OrderEvent("o-2", "dave", 500),
                new OrderEvent("o-3", "carol", 900), new OrderEvent("o-4", "alice", 300),
                new OrderEvent("o-5", "dave", 150), new OrderEvent("o-6", "carol", 4000));

        System.out.println("--- producer: send(topic, key, value).get() -> RecordMetadata ---");
        for (OrderEvent e : events) {
            RecordMetadata meta = producer.publish(e).join().getRecordMetadata();
            System.out.printf("%s key=%-5s -> %s-%d@%d%n", e.orderId(), e.customer(), meta.topic(), meta.partition(), meta.offset());
        }

        await().atMost(Duration.ofSeconds(30))
                .until(() -> received.of("shipping").size() == 6 && received.of("billing").size() == 6);

        System.out.println("--- group 'shipping' (concurrency 3) ---");
        received.of("shipping").stream().sorted().forEach(System.out::println);
        System.out.println("--- group 'billing' (concurrency 1) ---");
        received.of("billing").stream().sorted().forEach(System.out::println);

        // same key -> same partition
        Map<String, Long> partitionsPerCustomer = received.of("shipping").stream()
                .collect(Collectors.groupingBy(l -> l.split(" ")[1], Collectors.mapping(l -> l.split(" ")[2], Collectors.counting())));
        assertThat(received.of("shipping")).allSatisfy(l -> assertThat(l).contains("on shipping-"));
        assertThat(received.of("shipping").stream().map(l -> l.split(" ")[1] + l.split(" ")[2]).distinct()).hasSize(3);
        assertThat(partitionsPerCustomer).hasSize(3);
        // three partitions -> three different consumer threads did the work
        assertThat(received.of("shipping").stream().map(l -> l.substring(l.indexOf(" on "))).distinct()).hasSize(3);
        // one thread in 'billing' saw all six
        assertThat(received.of("billing")).allSatisfy(l -> assertThat(l).endsWith("on billing-0-C-1"));
    }
}
