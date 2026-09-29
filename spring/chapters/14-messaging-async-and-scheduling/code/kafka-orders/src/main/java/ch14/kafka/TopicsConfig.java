package ch14.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Boot's KafkaAdmin creates every NewTopic bean at startup (if it does not exist yet). */
@Configuration(proxyBeanMethods = false)
public class TopicsConfig {
    @Bean NewTopic orders()      { return TopicBuilder.name("orders").partitions(3).replicas(1).build(); }
    @Bean NewTopic payments()    { return TopicBuilder.name("payments").partitions(1).replicas(1).build(); }
    @Bean NewTopic paymentsDlt() { return TopicBuilder.name("payments-dlt").partitions(1).replicas(1).build(); }
}
