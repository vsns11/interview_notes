package ch22.blockingqueue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Way 2: one ReentrantLock with two Conditions, one per reason to wait.
 * Producers wait on notFull, consumers on notEmpty, so signal() wakes exactly
 * one thread of the right kind. This is the shape of java.util.concurrent.ArrayBlockingQueue.
 */
public final class LockConditionBuffer<T> implements BoundedBuffer<T> {
    private final Object[] items;
    private int head;
    private int count;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public LockConditionBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        items = new Object[capacity];
    }

    @Override
    public void put(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                notFull.await();
            }
            items[(head + count) % items.length] = item;
            count++;
            notEmpty.signal();               // exactly one consumer can use one new item
        } finally {
            lock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return removeHead();
        } finally {
            lock.unlock();
        }
    }

    /** Timed variant: returns null if nothing arrives within the timeout. */
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0L) return null;
                nanos = notEmpty.awaitNanos(nanos);   // returns the time still left
            }
            return removeHead();
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private T removeHead() {              // caller holds the lock
        T item = (T) items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        count--;
        notFull.signal();                 // exactly one producer can use one free slot
        return item;
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
        return "Lock + 2 Conditions";
    }
}
