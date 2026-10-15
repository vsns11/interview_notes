package ch22.deferredcallback;

import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Runs callbacks no earlier than their deadline, on one dedicated worker thread.
 *
 *   schedule(cb, delay)  any thread; adds (deadline, cb) to a min-heap ordered by deadline
 *   worker loop          sleeps until the EARLIEST deadline, or until woken because a new,
 *                        even earlier task arrived, then runs every task that is due
 *
 * Deadlines use System.nanoTime() (monotonic: unaffected by wall-clock changes).
 */
public final class DeferredCallbackExecutor implements AutoCloseable {

    private record Task(long deadlineNanos, long seq, Runnable callback) implements Comparable<Task> {
        @Override
        public int compareTo(Task o) {
            int c = Long.compare(deadlineNanos - o.deadlineNanos, 0);   // overflow-safe nanoTime compare
            return c != 0 ? c : Long.compare(seq, o.seq);              // tie -> first scheduled runs first
        }
    }

    private final PriorityQueue<Task> queue = new PriorityQueue<>();   // NOT thread-safe: guarded by lock
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();             // "the head of the queue may have changed"
    private final AtomicLong seq = new AtomicLong();
    private final Thread worker;
    private boolean shutdown;                                           // guarded by lock

    public DeferredCallbackExecutor() {
        worker = Thread.ofPlatform().name("deferred-callback-worker").unstarted(this::runLoop);
        worker.start();   // last statement of a final class's constructor: every field is already assigned
    }

    public void schedule(Runnable callback, long delay, TimeUnit unit) {
        Task task = new Task(System.nanoTime() + unit.toNanos(delay), seq.getAndIncrement(), callback);
        lock.lock();
        try {
            if (shutdown) throw new IllegalStateException("executor is shut down");
            queue.add(task);
            if (queue.peek() == task) {
                changed.signal();          // new earliest deadline: the worker must recompute its sleep
            }
        } finally {
            lock.unlock();
        }
    }

    private void runLoop() {
        while (true) {
            Runnable due;
            try {
                due = takeNextDue();
            } catch (InterruptedException e) {
                return;
            }
            if (due == null) return;                       // shut down and drained
            try {
                due.run();                                  // OUTSIDE the lock: a slow or re-entrant callback can't block schedule()
            } catch (RuntimeException e) {
                System.out.println("   [worker] callback threw " + e + " - worker keeps going");
            }
        }
    }

    /** Blocks until the head task is due; returns null once shut down and the queue is empty. */
    private Runnable takeNextDue() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                Task head = queue.peek();
                if (head == null) {
                    if (shutdown) return null;
                    changed.await();                        // nothing to do: sleep until schedule()/close()
                    continue;
                }
                long waitNanos = head.deadlineNanos() - System.nanoTime();
                if (waitNanos <= 0) {
                    return queue.poll().callback();         // due now
                }
                changed.awaitNanos(waitNanos);              // timed wait on the earliest deadline;
                                                            // loop re-reads the head whatever woke us
            }
        } finally {
            lock.unlock();
        }
    }

    /** Stops accepting tasks, runs the ones already scheduled when they fall due, then stops the worker. */
    @Override
    public void close() {
        lock.lock();
        try {
            shutdown = true;
            changed.signal();
        } finally {
            lock.unlock();
        }
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // caller was interrupted: keep the flag, stop waiting
        }
    }
}
