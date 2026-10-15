package ch22.tokenbucket;

import java.util.concurrent.TimeUnit;

/**
 * Blocking token bucket refilled by a BACKGROUND daemon thread.
 * The constructor is private: callers use the static factory start(...), which first
 * finishes building the object and only then starts the refill thread.
 */
public final class BackgroundRefillBucket implements TokenBucket, AutoCloseable {
    private final int capacity;
    private final long nanosPerToken;
    private int tokens;                       // guarded by 'this'
    private final Thread refiller;            // assigned in the constructor, started by the factory

    private BackgroundRefillBucket(int capacity, int tokensPerSecond) {
        if (capacity < 1 || tokensPerSecond < 1) throw new IllegalArgumentException();
        this.capacity = capacity;
        this.nanosPerToken = TimeUnit.SECONDS.toNanos(1) / tokensPerSecond;
        this.tokens = capacity;
        this.refiller = new Thread(this::refillLoop, "token-refiller");   // created, NOT started
        this.refiller.setDaemon(true);        // must not keep the JVM alive on its own
    }

    /** Factory: construct fully, then start the thread. 'this' never escapes a half-built object. */
    public static BackgroundRefillBucket start(int capacity, int tokensPerSecond) {
        BackgroundRefillBucket bucket = new BackgroundRefillBucket(capacity, tokensPerSecond);
        bucket.refiller.start();              // start() happens-before everything the new thread does
        return bucket;
    }

    private void refillLoop() {
        long next = System.nanoTime() + nanosPerToken;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                long sleep = next - System.nanoTime();
                if (sleep > 0) TimeUnit.NANOSECONDS.sleep(sleep);  // deadline-based: no drift
                next += nanosPerToken;
                synchronized (this) {
                    if (tokens < capacity) {
                        tokens++;
                        notify();             // one new token can satisfy exactly one waiter
                    }
                }
            }
        } catch (InterruptedException e) {
            // close() interrupted us: fall through and let the thread end
        }
    }

    @Override
    public synchronized void getToken() throws InterruptedException {
        while (tokens == 0) {
            wait();                           // releases the monitor; the refiller notifies
        }
        tokens--;
    }

    @Override
    public synchronized boolean tryGetToken() {
        if (tokens == 0) return false;
        tokens--;
        return true;
    }

    /** Stops the refill thread. Threads still blocked in getToken() would wait forever, so stop callers first. */
    @Override
    public void close() {
        refiller.interrupt();
        try {
            refiller.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // keep the caller's interrupt status
        }
    }

    public boolean refillerAlive() { return refiller.isAlive(); }
}
