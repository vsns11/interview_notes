package ch22.barbershop;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Sleeping barber: one barber, one barber chair, N waiting chairs.
 *  - No customers: the barber sleeps (blocks) until one arrives.
 *  - Customer arrives, all waiting chairs taken: the customer leaves at once.
 *  - Otherwise the customer sits down, wakes the barber if needed, and waits until
 *    HIS OWN haircut is finished.
 *
 * Primitives: one ReentrantLock guards the queue of waiting customers; one Condition
 * ("customerWaiting") is where the barber sleeps; each customer gets a CountDownLatch(1)
 * that the barber counts down when that customer's haircut is done.
 */
public final class BarberShop {
    private record Ticket(String customer, CountDownLatch done) {}

    private final int waitingChairs;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition customerWaiting = lock.newCondition();
    private final Deque<Ticket> chairs = new ArrayDeque<>();   // guarded by lock; FIFO
    private boolean closed;                                     // guarded by lock

    public BarberShop(int waitingChairs) { this.waitingChairs = waitingChairs; }

    /** Customer side. Returns false if the shop was full (customer turned away). */
    public boolean visit(String customer) throws InterruptedException {
        Ticket ticket = new Ticket(customer, new CountDownLatch(1));
        lock.lock();
        try {
            if (closed || chairs.size() == waitingChairs) {
                return false;                        // never block on a full shop: just leave
            }
            chairs.addLast(ticket);
            customerWaiting.signal();                // only one barber waits here, so signal() is enough
        } finally {
            lock.unlock();
        }
        ticket.done().await();                       // wait OUTSIDE the lock for my haircut to end
        return true;
    }

    /** Barber side: runs until close() and every seated customer has been served. */
    public void barberLoop(Consumer<String> cutHair, Runnable onSleep) throws InterruptedException {
        while (true) {
            Ticket next;
            lock.lock();
            try {
                if (chairs.isEmpty() && !closed) onSleep.run();
                while (chairs.isEmpty() && !closed) {
                    customerWaiting.await();         // sleep; releases the lock; re-check on wakeup
                }
                if (chairs.isEmpty()) return;        // closed and nobody left to serve
                next = chairs.removeFirst();         // frees a waiting chair immediately
            } finally {
                lock.unlock();
            }
            cutHair.accept(next.customer());         // the slow part runs WITHOUT the lock
            next.done().countDown();                 // let this customer go home
        }
    }

    /** No new customers accepted; the barber finishes the queue, then returns. */
    public void close() {
        lock.lock();
        try {
            closed = true;
            customerWaiting.signal();
        } finally {
            lock.unlock();
        }
    }
}
