package lld.ch13.blockingqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** A bounded blocking queue built two ways: monitor (wait/notifyAll) and Lock + two Conditions. */
public class Main {

    interface BoundedBlockingQueue<T> {
        void put(T item) throws InterruptedException;          // blocks while full
        T take() throws InterruptedException;                  // blocks while empty
        boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException; // false on timeout
        T poll(long timeout, TimeUnit unit) throws InterruptedException;                // null on timeout
        int size();
    }

    // ------------------------------------------------------------------
    // Way 1: intrinsic lock. Producers AND consumers wait on the SAME monitor,
    // so we must use notifyAll() (notify() could wake the wrong kind of thread).
    // ------------------------------------------------------------------
    static final class MonitorQueue<T> implements BoundedBlockingQueue<T> {
        private final Object[] items;
        private int head, tail, count;                          // all guarded by 'this'

        MonitorQueue(int capacity) { items = new Object[capacity]; }

        public synchronized void put(T item) throws InterruptedException {
            while (count == items.length) wait();
            enqueue(item);
            notifyAll();
        }
        public synchronized T take() throws InterruptedException {
            while (count == 0) wait();
            T x = dequeue();
            notifyAll();
            return x;
        }
        public synchronized boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
            long deadline = System.nanoTime() + unit.toNanos(timeout);
            while (count == items.length) {
                long left = deadline - System.nanoTime();
                if (left <= 0) return false;
                TimeUnit.NANOSECONDS.timedWait(this, left);      // wait(millis, nanos) under the hood
            }
            enqueue(item);
            notifyAll();
            return true;
        }
        public synchronized T poll(long timeout, TimeUnit unit) throws InterruptedException {
            long deadline = System.nanoTime() + unit.toNanos(timeout);
            while (count == 0) {
                long left = deadline - System.nanoTime();
                if (left <= 0) return null;
                TimeUnit.NANOSECONDS.timedWait(this, left);
            }
            T x = dequeue();
            notifyAll();
            return x;
        }
        public synchronized int size() { return count; }

        private void enqueue(T item) {
            if (item == null) throw new NullPointerException();  // null is our "timed out" signal
            items[tail] = item;
            tail = (tail + 1) % items.length;                    // circular buffer
            count++;
        }
        @SuppressWarnings("unchecked")
        private T dequeue() {
            T x = (T) items[head];
            items[head] = null;                                  // let GC reclaim it
            head = (head + 1) % items.length;
            count--;
            return x;
        }
    }

    // ------------------------------------------------------------------
    // Way 2: ReentrantLock with TWO condition queues. Producers wait on notFull,
    // consumers on notEmpty, so a targeted signal() is enough.
    // (This is how java.util.concurrent.ArrayBlockingQueue is built.)
    // ------------------------------------------------------------------
    static final class ConditionQueue<T> implements BoundedBlockingQueue<T> {
        private final Object[] items;
        private int head, tail, count;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        ConditionQueue(int capacity) { items = new Object[capacity]; }

        public void put(T item) throws InterruptedException {
            if (item == null) throw new NullPointerException();
            lock.lockInterruptibly();
            try {
                while (count == items.length) notFull.await();
                enqueue(item);
            } finally { lock.unlock(); }
        }
        public T take() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                while (count == 0) notEmpty.await();
                return dequeue();
            } finally { lock.unlock(); }
        }
        public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
            if (item == null) throw new NullPointerException();
            long nanos = unit.toNanos(timeout);
            lock.lockInterruptibly();
            try {
                while (count == items.length) {
                    if (nanos <= 0) return false;
                    nanos = notFull.awaitNanos(nanos);           // returns the time still left
                }
                enqueue(item);
                return true;
            } finally { lock.unlock(); }
        }
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            lock.lockInterruptibly();
            try {
                while (count == 0) {
                    if (nanos <= 0) return null;
                    nanos = notEmpty.awaitNanos(nanos);
                }
                return dequeue();
            } finally { lock.unlock(); }
        }
        public int size() { lock.lock(); try { return count; } finally { lock.unlock(); } }

        private void enqueue(T item) {
            items[tail] = item;
            tail = (tail + 1) % items.length;
            count++;
            notEmpty.signal();                                   // wake ONE waiting consumer
        }
        @SuppressWarnings("unchecked")
        private T dequeue() {
            T x = (T) items[head];
            items[head] = null;
            head = (head + 1) % items.length;
            count--;
            notFull.signal();                                    // wake ONE waiting producer
            return x;
        }
    }

    // ------------------------------------------------------------------
    static void stressTest(String name, BoundedBlockingQueue<Integer> q) throws InterruptedException {
        final int PRODUCERS = 3, CONSUMERS = 3, PER_PRODUCER = 20_000;
        AtomicLong consumedSum = new AtomicLong(), consumedCount = new AtomicLong();
        List<Thread> ts = new ArrayList<>();
        for (int p = 0; p < PRODUCERS; p++) {
            Thread t = new Thread(() -> {
                try { for (int i = 1; i <= PER_PRODUCER; i++) q.put(i); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            ts.add(t);
        }
        int perConsumer = PRODUCERS * PER_PRODUCER / CONSUMERS;
        for (int c = 0; c < CONSUMERS; c++) {
            Thread t = new Thread(() -> {
                try { for (int i = 0; i < perConsumer; i++) { consumedSum.addAndGet(q.take()); consumedCount.incrementAndGet(); } }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            ts.add(t);
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        long expected = (long) PRODUCERS * PER_PRODUCER * (PER_PRODUCER + 1) / 2;
        System.out.println(name + ": consumed " + consumedCount.get() + " items, sum ok=" + (consumedSum.get() == expected)
                + ", left in queue=" + q.size());

        System.out.println(name + ": poll(30ms) on empty -> " + q.poll(30, TimeUnit.MILLISECONDS));
        q.put(1); q.put(2);                                      // capacity is 2 in this demo
        System.out.println(name + ": offer(30ms) on full -> " + q.offer(3, 30, TimeUnit.MILLISECONDS));
    }

    public static void main(String[] args) throws InterruptedException {
        stressTest("wait/notifyAll  ", new MonitorQueue<>(2));
        stressTest("Lock+Conditions ", new ConditionQueue<>(2));
    }
}
