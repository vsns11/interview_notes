package lld.ch11.architecture.adapter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lld.ch11.architecture.domain.OrderEvent;
import lld.ch11.architecture.port.EventPublisher;

/**
 * DRIVEN adapter: a toy BROKER (event-driven, broker topology). Publishers and
 * subscribers know only the bus and the event types, never each other.
 * Synchronous and in memory; Kafka or RabbitMQ would add durability and async delivery.
 */
public final class InMemoryEventBus implements EventPublisher {
    private record Subscription<T>(Class<T> type, Consumer<? super T> handler) {
        void offer(Object event) { if (type.isInstance(event)) handler.accept(type.cast(event)); }
    }

    private final List<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    public <T extends OrderEvent> void subscribe(Class<T> type, Consumer<? super T> handler) {
        subscriptions.add(new Subscription<>(type, handler));
    }

    @Override
    public void publish(OrderEvent event) { subscriptions.forEach(s -> s.offer(event)); }
}
