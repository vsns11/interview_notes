package ch22.semaphore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A FIFO-fair counting semaphore built on ReentrantLock + Condition.
 * Each waiting thread gets its own ticket in a queue; only the thread at the head of
 * the queue may take a permit, so a newcomer can never "barge" past a waiting thread.
 * (A fair ReentrantLock alone is NOT enough: it orders who gets the LOCK, not who gets the PERMIT.)
 */
public final class FairSemaphore {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private final Deque<Thread> queue = new ArrayDeque<>();
    private int permits;

    public FairSemaphore(int permits) {
        if (permits < 0) throw new IllegalArgumentException("permits must be >= 0");
        this.permits = permits;
    }

    public void acquire() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            Thread me = Thread.currentThread();
            queue.addLast(me);
            try {
                while (queue.peekFirst() != me || permits == 0) {
                    changed.await();
                }
                permits--;
            } finally {
                queue.remove(me);       // normal exit or interrupt: leave the queue either way
                changed.signalAll();    // the next thread in line may now be at the head
            }
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            permits++;
            changed.signalAll();        // wake everyone; only the head can proceed
        } finally {
            lock.unlock();
        }
    }

    public int availablePermits() {
        lock.lock();
        try {
            return permits;
        } finally {
            lock.unlock();
        }
    }
}
