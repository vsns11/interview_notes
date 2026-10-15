package ch22.semaphore;

import java.util.concurrent.TimeUnit;

/**
 * A counting semaphore built on the object's monitor (synchronized + wait/notifyAll).
 *
 * State: one int, the number of available permits. Invariant: permits >= 0.
 *   acquire(n) waits until permits >= n, then subtracts n.
 *   release(n) adds n and wakes waiters.
 * Like java.util.concurrent.Semaphore, there is no owner: any thread may release,
 * and release() may raise the count above its initial value. Pass maxPermits to
 * forbid that (release beyond the bound throws: it is a bug in the caller).
 */
public final class CountingSemaphore {
    private int permits;
    private final int maxPermits;

    public CountingSemaphore(int initialPermits) {
        this(initialPermits, Integer.MAX_VALUE);
    }

    public CountingSemaphore(int initialPermits, int maxPermits) {
        if (initialPermits < 0 || initialPermits > maxPermits)
            throw new IllegalArgumentException("need 0 <= initial <= max");
        this.permits = initialPermits;
        this.maxPermits = maxPermits;
    }

    public void acquire() throws InterruptedException {
        acquire(1);
    }

    public synchronized void acquire(int n) throws InterruptedException {
        checkCount(n);
        while (permits < n) {          // while, not if: spurious wakeups and barging threads
            wait();
        }
        permits -= n;
    }

    /** Takes a permit only if one is free right now; never blocks. */
    public synchronized boolean tryAcquire() {
        if (permits == 0) return false;
        permits--;
        return true;
    }

    /** Waits at most the given time; returns false on timeout. */
    public synchronized boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (permits == 0) {
            long leftNanos = deadline - System.nanoTime();
            if (leftNanos <= 0) return false;
            long ms = TimeUnit.NANOSECONDS.toMillis(leftNanos);
            int ns = (int) (leftNanos - TimeUnit.MILLISECONDS.toNanos(ms));
            wait(ms, ns == 0 && ms == 0 ? 1 : ns);   // wait(0, 0) would mean "forever"
        }
        permits--;
        return true;
    }

    public void release() {
        release(1);
    }

    public synchronized void release(int n) {
        checkCount(n);
        if (permits > maxPermits - n)                 // overflow-safe form of permits + n > max
            throw new IllegalStateException("release would exceed maxPermits=" + maxPermits);
        permits += n;
        notifyAll();                  // waiters may want different n, so wake them all to re-check
    }

    public synchronized int availablePermits() {
        return permits;
    }

    private static void checkCount(int n) {
        if (n <= 0) throw new IllegalArgumentException("permit count must be > 0");
    }
}
