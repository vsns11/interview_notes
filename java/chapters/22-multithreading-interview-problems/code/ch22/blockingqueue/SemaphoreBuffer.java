package ch22.blockingqueue;

import java.util.concurrent.Semaphore;

/**
 * Way 3: two counting semaphores plus a mutex.
 *   freeSlots  starts at capacity: a producer must take one before inserting.
 *   filledSlots starts at 0:       a consumer must take one before removing.
 *   mutex (a binary semaphore) protects the array indices themselves.
 * The counting semaphores are acquired BEFORE the mutex: acquiring the mutex first
 * and then blocking on a counting semaphore would hold the mutex while asleep -> deadlock.
 */
public final class SemaphoreBuffer<T> implements BoundedBuffer<T> {
    private final Object[] items;
    private int head;
    private int tail;
    private final Semaphore freeSlots;
    private final Semaphore filledSlots = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    public SemaphoreBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        items = new Object[capacity];
        freeSlots = new Semaphore(capacity);
    }

    @Override
    public void put(T item) throws InterruptedException {
        freeSlots.acquire();                 // 1) reserve a free slot (may block)
        try {
            mutex.acquire();                 // 2) exclusive access to the array
        } catch (InterruptedException e) {
            freeSlots.release();             // give the reservation back, or capacity leaks
            throw e;
        }
        try {
            items[tail] = item;
            tail = (tail + 1) % items.length;
        } finally {
            mutex.release();
        }
        filledSlots.release();               // 3) announce one more item
    }

    @Override
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        filledSlots.acquire();               // 1) reserve an item (may block)
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            filledSlots.release();
            throw e;
        }
        T item;
        try {
            item = (T) items[head];
            items[head] = null;
            head = (head + 1) % items.length;
        } finally {
            mutex.release();
        }
        freeSlots.release();                 // 3) announce one more free slot
        return item;
    }

    @Override
    public int size() {
        return filledSlots.availablePermits();   // approximate while threads are mid-operation
    }

    @Override
    public String name() {
        return "semaphores (free/filled + mutex)";
    }
}
