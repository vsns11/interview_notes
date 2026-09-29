package ch14.outbox;

/** The broker, seen from the poller. In production: KafkaTemplate.send(...).get() (see README). */
public interface MessagePublisher {
    void publish(Message message) throws Exception;
}
