package lld.ch20.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** One published message. The offset is its position in the topic's log. */
record Message(String topic, long offset, String key, String payload) {}

/**
 * An append-only log (like one Kafka partition). Messages are never removed by consumption:
 * each subscription just remembers how far it has read (its offset).
 */
final class Topic {
    private final String name;
    private final List<Message> log = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition appended = lock.newCondition();

    Topic(String name) { this.name = name; }

    String name() { return name; }

    long append(String key, String payload) {
        lock.lock();
        try {
            long offset = log.size();
            log.add(new Message(name, offset, key, payload));
            appended.signalAll();                  // wake every waiting dispatcher
            return offset;
        } finally {
            lock.unlock();
        }
    }

    /** Returns the message at 'offset', waiting up to the timeout for it to be published. */
    Message awaitAt(long offset, long timeoutMs) throws InterruptedException {
        lock.lock();
        try {
            long nanos = TimeUnit.MILLISECONDS.toNanos(timeoutMs);
            while (offset >= log.size()) {         // loop: guards against spurious wake-ups
                if (nanos <= 0) return null;
                nanos = appended.awaitNanos(nanos);
            }
            return log.get((int) offset);
        } finally {
            lock.unlock();
        }
    }

    long endOffset() {
        lock.lock();
        try {
            return log.size();
        } finally {
            lock.unlock();
        }
    }
}
