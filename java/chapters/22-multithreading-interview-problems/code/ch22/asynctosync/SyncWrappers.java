package ch22.asynctosync;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Four ways to turn AsyncService.fetch(key, callback) into a blocking
 *     String fetch(key, timeout) throws InterruptedException, ExecutionException, TimeoutException
 * Common rules: the "mailbox" is created PER CALL; completion is recorded in state (not just a
 * signal), so a callback that runs before the caller starts waiting is not missed; the first
 * completion wins; the wait has a timeout; failures surface as ExecutionException(cause).
 */
public final class SyncWrappers {
    private SyncWrappers() {}

    public interface SyncFetcher {
        String fetch(String key, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException;

        String name();
    }

    // ------------------------------------------------------------------ 1. wait / notifyAll
    public static final class MonitorSync implements SyncFetcher {
        private final AsyncService service;
        public MonitorSync(AsyncService service) { this.service = service; }

        private static final class Mailbox {          // one per call; its monitor guards its fields
            boolean done;
            String value;
            Throwable error;

            synchronized void complete(String v, Throwable e) {
                if (done) return;                      // first completion wins
                value = v;
                error = e;
                done = true;                           // the STATE that makes a missed signal impossible
                notifyAll();
            }
        }

        @Override
        public String fetch(String key, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
            Mailbox box = new Mailbox();
            service.fetch(key, new AsyncService.Callback<>() {
                public void onSuccess(String v) { box.complete(v, null); }
                public void onFailure(Throwable e) { box.complete(null, e); }
            });
            long deadline = System.nanoTime() + timeout.toNanos();
            synchronized (box) {
                while (!box.done) {                    // checks state first: fine if the callback already ran
                    long left = deadline - System.nanoTime();
                    if (left <= 0) throw new TimeoutException("no reply within " + timeout.toMillis() + " ms");
                    TimeUnit.NANOSECONDS.timedWait(box, left);
                }
                if (box.error != null) throw new ExecutionException(box.error);
                return box.value;
            }
        }

        public String name() { return "wait/notifyAll"; }
    }

    // ------------------------------------------------------------------ 2. Lock + Condition
    public static final class ConditionSync implements SyncFetcher {
        private final AsyncService service;
        public ConditionSync(AsyncService service) { this.service = service; }

        private static final class Mailbox {
            final ReentrantLock lock = new ReentrantLock();
            final Condition finished = lock.newCondition();
            boolean done;                              // these three are guarded by lock
            String value;
            Throwable error;

            void complete(String v, Throwable e) {
                lock.lock();
                try {
                    if (done) return;                  // first completion wins
                    value = v;
                    error = e;
                    done = true;
                    finished.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public String fetch(String key, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
            Mailbox box = new Mailbox();
            service.fetch(key, new AsyncService.Callback<>() {
                public void onSuccess(String v) { box.complete(v, null); }
                public void onFailure(Throwable e) { box.complete(null, e); }
            });
            long nanos = timeout.toNanos();
            box.lock.lockInterruptibly();
            try {
                while (!box.done) {
                    if (nanos <= 0) throw new TimeoutException("no reply within " + timeout.toMillis() + " ms");
                    nanos = box.finished.awaitNanos(nanos);
                }
                if (box.error != null) throw new ExecutionException(box.error);
                return box.value;
            } finally {
                box.lock.unlock();
            }
        }

        public String name() { return "Lock + Condition"; }
    }

    // ------------------------------------------------------------------ 3. CountDownLatch
    public static final class LatchSync implements SyncFetcher {
        private final AsyncService service;
        public LatchSync(AsyncService service) { this.service = service; }

        private record Outcome(String value, Throwable error) {}

        @Override
        public String fetch(String key, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Outcome> result = new AtomicReference<>();
            service.fetch(key, new AsyncService.Callback<>() {
                public void onSuccess(String v) { if (result.compareAndSet(null, new Outcome(v, null))) latch.countDown(); }
                public void onFailure(Throwable e) { if (result.compareAndSet(null, new Outcome(null, e))) latch.countDown(); }
            });
            if (!latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS))
                throw new TimeoutException("no reply within " + timeout.toMillis() + " ms");
            Outcome o = result.get();                  // countDown() happens-before await() returning
            if (o.error() != null) throw new ExecutionException(o.error());
            return o.value();
        }

        public String name() { return "CountDownLatch"; }
    }

    // ------------------------------------------------------------------ 4. CompletableFuture
    public static final class FutureSync implements SyncFetcher {
        private final AsyncService service;
        public FutureSync(AsyncService service) { this.service = service; }

        /** The reusable bridge: callback API -> CompletableFuture. Callers may block OR compose. */
        public CompletableFuture<String> fetchAsync(String key) {
            CompletableFuture<String> f = new CompletableFuture<>();
            service.fetch(key, new AsyncService.Callback<>() {
                public void onSuccess(String v) { f.complete(v); }               // first call wins; later ones return false
                public void onFailure(Throwable e) { f.completeExceptionally(e); }
            });
            return f;
        }

        @Override
        public String fetch(String key, Duration timeout) throws InterruptedException, ExecutionException, TimeoutException {
            CompletableFuture<String> f = fetchAsync(key);
            try {
                return f.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                f.cancel(false);                       // late callback now finds the future already completed
                throw e;
            }
        }

        public String name() { return "CompletableFuture"; }
    }
}
