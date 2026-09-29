package lld.ch11.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A tiny in-process event bus: publishers and subscribers share only event TYPES,
 * never references to each other. Synchronous, not durable (unlike Kafka).
 */
public final class EventBus {
    private record Handler<E>(Class<E> type, Consumer<? super E> action) {
        void deliver(Object event) {
            if (type.isInstance(event)) action.accept(type.cast(event));
        }
    }

    /** AutoCloseable so try-with-resources can unsubscribe for you. */
    public interface Subscription extends AutoCloseable {
        @Override void close();
    }

    private final List<Handler<?>> handlers = new CopyOnWriteArrayList<>();

    public <E> Subscription subscribe(Class<E> type, Consumer<? super E> action) {
        Handler<E> h = new Handler<>(type, action);
        handlers.add(h);
        return () -> handlers.remove(h);
    }

    public void publish(Object event) {
        handlers.forEach(h -> h.deliver(event));
    }
}
