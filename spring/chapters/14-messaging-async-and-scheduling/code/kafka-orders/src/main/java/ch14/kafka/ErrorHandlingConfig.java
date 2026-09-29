package ch14.kafka;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class ErrorHandlingConfig {

    /**
     * Boot applies a CommonErrorHandler bean to its auto-configured listener container factory.
     * Blocking retries: 2 more tries 100 ms apart (3 attempts in total), then publish to <topic>-dlt.
     */
    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> jsonTemplate, ProducerFactory<String, Object> pf) {
        // A record that could not be deserialized has only raw bytes: publish those bytes unchanged.
        KafkaTemplate<String, Object> bytesTemplate =
                new KafkaTemplate<>(pf, Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, jsonTemplate);

        var recoverer = new DeadLetterPublishingRecoverer(templates);   // default target: same topic + "-dlt", same partition
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(100, 2));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);   // bad data will never get better
        return handler;
    }
}
