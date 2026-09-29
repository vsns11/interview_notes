package lld.ch20.pubsub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Observer (push style): the broker calls this for every message, in order. Throw = negative ack. */
interface Subscriber {
    void onMessage(Message m) throws Exception;
}

enum StartFrom { EARLIEST, LATEST }

/**
 * One subscriber's view of one topic: its committed offset and its own dispatcher thread.
 * Delivery is at-least-once and in order. A message is acknowledged (offset committed)
 * only after onMessage returns normally. After maxAttempts failures it goes to the dead-letter topic.
 */
final class Subscription {
    private final String id;
    private final Topic topic;
    private final Subscriber subscriber;
    private final int maxAttempts;
    private final Topic deadLetters;
    private final AtomicLong committed;        // next offset to deliver
    private volatile boolean running = true;
    private final Thread dispatcher;

    Subscription(String id, Topic topic, Subscriber subscriber, long startOffset, int maxAttempts, Topic deadLetters) {
        this.id = id;
        this.topic = topic;
        this.subscriber = subscriber;
        this.maxAttempts = maxAttempts;
        this.deadLetters = deadLetters;
        this.committed = new AtomicLong(startOffset);
        this.dispatcher = Thread.ofVirtual().name("dispatch-" + id).start(this::loop);
    }

    private void loop() {
        try {
            while (running) {
                long offset = committed.get();
                Message m = topic.awaitAt(offset, 50);
                if (m == null) continue;              // timed out: re-check 'running' and any seek()
                deliver(m);
                committed.compareAndSet(offset, offset + 1); // ack; a concurrent seek() wins
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void deliver(Message m) throws InterruptedException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                subscriber.onMessage(m);
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    deadLetters.append(id, m.topic() + "@" + m.offset() + " " + m.payload() + " -> " + e.getMessage());
                    return;                           // skip it so one poison message cannot block the rest
                }
                Thread.sleep(5L * attempt);           // simple backoff
            }
        }
    }

    String id() { return id; }
    long committedOffset() { return committed.get(); }
    boolean caughtUp() { return committed.get() >= topic.endOffset(); }

    /** Replay (or skip) by moving the offset - possible because the log keeps every message. */
    void seek(long offset) { committed.set(offset); }

    void close() throws InterruptedException {
        running = false;
        dispatcher.join();
    }
}

/** Topic registry + publish + subscribe. Publishers never know who the subscribers are. */
final class Broker implements AutoCloseable {
    static final String DLQ = "__dead_letters";
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    Broker() { createTopic(DLQ); }

    Topic createTopic(String name) { return topics.computeIfAbsent(name, Topic::new); }

    long publish(String topic, String key, String payload) {
        Topic t = topics.get(topic);
        if (t == null) throw new IllegalArgumentException("no topic " + topic);
        return t.append(key, payload);
    }

    Subscription subscribe(String topic, String subscriptionId, Subscriber s, StartFrom from) {
        Topic t = topics.get(topic);
        if (t == null) throw new IllegalArgumentException("no topic " + topic);
        long start = from == StartFrom.EARLIEST ? 0 : t.endOffset();
        Subscription sub = new Subscription(subscriptionId, t, s, start, 3, topics.get(DLQ));
        subscriptions.add(sub);
        return sub;
    }

    void unsubscribe(Subscription s) throws InterruptedException {
        subscriptions.remove(s);
        s.close();
    }

    /** Test/shutdown helper: wait until every subscription has acknowledged everything. */
    boolean awaitIdle(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (subscriptions.stream().allMatch(Subscription::caughtUp)) return true;
            Thread.sleep(5);
        }
        return false;
    }

    Topic topic(String name) { return topics.get(name); }

    /** Stops every dispatcher. Declared without InterruptedException so try-with-resources stays simple. */
    @Override
    public void close() {
        try {
            for (Subscription s : subscriptions) s.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();      // keep the interrupt visible to the caller
        }
    }
}
