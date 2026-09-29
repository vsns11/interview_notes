package lld.ch13.philosophers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dining Philosophers (LeetCode 1226 variant): 5 philosophers, 5 forks, each needs both
 * neighbouring forks to eat. Naive "everyone picks up the left fork first" can deadlock.
 */
public class Main {

    interface Table { void eat(int p, Runnable eatAction) throws InterruptedException; }

    /** Fix 1 - resource ordering: always pick up the LOWER-numbered fork first (breaks circular wait). */
    static class OrderedForks implements Table {
        private final ReentrantLock[] forks;
        OrderedForks(int n) { forks = new ReentrantLock[n]; for (int i = 0; i < n; i++) forks[i] = new ReentrantLock(); }

        public void eat(int p, Runnable eatAction) {
            int left = p, right = (p + 1) % forks.length;
            ReentrantLock first = forks[Math.min(left, right)], second = forks[Math.max(left, right)];
            first.lock();
            try {
                second.lock();
                try { eatAction.run(); } finally { second.unlock(); }
            } finally { first.unlock(); }
        }
    }

    /** Fix 2 - a "waiter" lets at most n-1 philosophers try at once (breaks circular wait too). */
    static class Waiter implements Table {
        private final ReentrantLock[] forks;
        private final Semaphore seats;
        Waiter(int n) {
            forks = new ReentrantLock[n];
            for (int i = 0; i < n; i++) forks[i] = new ReentrantLock();
            seats = new Semaphore(n - 1, true);                 // fair: nobody starves waiting for a seat
        }
        public void eat(int p, Runnable eatAction) throws InterruptedException {
            seats.acquire();
            try {
                ReentrantLock left = forks[p], right = forks[(p + 1) % forks.length];
                left.lock();
                try {
                    right.lock();
                    try { eatAction.run(); } finally { right.unlock(); }
                } finally { left.unlock(); }
            } finally { seats.release(); }
        }
    }

    static String dine(Table table, int n, int meals) throws InterruptedException {
        AtomicIntegerArray eaten = new AtomicIntegerArray(n);
        List<Thread> ts = new ArrayList<>();
        for (int p = 0; p < n; p++) {
            final int id = p;
            Thread t = new Thread(() -> {
                try {
                    for (int m = 0; m < meals; m++) {
                        Thread.onSpinWait();                               // "think"
                        table.eat(id, () -> eaten.incrementAndGet(id));
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            ts.add(t); t.start();
        }
        for (Thread t : ts) t.join();
        return eaten.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("resource ordering, meals per philosopher: " + dine(new OrderedForks(5), 5, 10_000));
        System.out.println("waiter (4 seats),  meals per philosopher: " + dine(new Waiter(5), 5, 10_000));
    }
}
