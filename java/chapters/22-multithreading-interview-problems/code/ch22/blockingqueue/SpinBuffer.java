package ch22.blockingqueue;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Way 4 (the one to criticise): busy-waiting. The thread repeatedly takes the lock,
 * checks the condition, releases the lock and tries again. It is CORRECT (the check
 * and the update happen under one lock) but a waiting thread burns a whole CPU core
 * doing nothing useful, and it competes for the lock with the threads doing real work.
 */
public final class SpinBuffer<T> implements BoundedBuffer<T> {
    private final Object[] items;
    private int head;
    private int count;
    private final ReentrantLock lock = new ReentrantLock();

    public SpinBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        items = new Object[capacity];
    }

    @Override
    public void put(T item) throws InterruptedException {
        while (true) {
            if (Thread.interrupted()) throw new InterruptedException();  // nothing else would notice
            lock.lock();
            try {
                if (count < items.length) {          // check AND act under the same lock
                    items[(head + count) % items.length] = item;
                    count++;
                    return;
                }
            } finally {
                lock.unlock();
            }
            Thread.onSpinWait();                     // CPU hint only; the thread keeps running
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        while (true) {
            if (Thread.interrupted()) throw new InterruptedException();
            lock.lock();
            try {
                if (count > 0) {
                    T item = (T) items[head];
                    items[head] = null;
                    head = (head + 1) % items.length;
                    count--;
                    return item;
                }
            } finally {
                lock.unlock();
            }
            Thread.onSpinWait();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String name() {
        return "busy-wait (spin + lock)";
    }
}
