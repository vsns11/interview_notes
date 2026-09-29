package ch14.outbox;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/** Stand-in for Kafka so the pattern runs anywhere. Can simulate an outage or a lost acknowledgement. */
@Component
public class InMemoryBroker implements MessagePublisher {
    public enum Mode { UP, DOWN, ACK_LOST }

    private final List<Message> delivered = new CopyOnWriteArrayList<>();
    private volatile Mode mode = Mode.UP;

    @Override
    public void publish(Message m) throws Exception {
        switch (mode) {
            case DOWN -> throw new java.io.IOException("broker unreachable");
            case ACK_LOST -> {                       // stored, but the reply never arrives
                delivered.add(m);
                throw new java.util.concurrent.TimeoutException("no ack from broker");
            }
            case UP -> delivered.add(m);
        }
    }

    public void setMode(Mode mode) { this.mode = mode; }
    public List<Message> delivered() { return List.copyOf(delivered); }
    public void clear() { delivered.clear(); mode = Mode.UP; }
}
