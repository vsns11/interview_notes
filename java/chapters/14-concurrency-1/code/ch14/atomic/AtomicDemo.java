package ch14.atomic;

import java.util.concurrent.atomic.*;

/** Lock-free building blocks: compare-and-set and friends. */
public class AtomicDemo {
    record Range(int low, int high) {}

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger n = new AtomicInteger(10);
        System.out.println("incrementAndGet -> " + n.incrementAndGet());    // ++n
        System.out.println("getAndIncrement -> " + n.getAndIncrement() + " (now " + n.get() + ")"); // n++
        System.out.println("addAndGet(5)    -> " + n.addAndGet(5));
        System.out.println("CAS 17->100     -> " + n.compareAndSet(17, 100) + ", value " + n.get());
        System.out.println("CAS 17->200     -> " + n.compareAndSet(17, 200) + ", value " + n.get() + " (expected value was stale)");
        System.out.println("updateAndGet x2 -> " + n.updateAndGet(x -> x * 2));
        System.out.println("accumulateAndGet(max with 50) -> " + n.accumulateAndGet(50, Math::max));

        // What updateAndGet does internally: a CAS retry loop
        AtomicInteger hits = new AtomicInteger();
        int prev, next;
        do {
            prev = hits.get();
            next = prev + 1;
        } while (!hits.compareAndSet(prev, next));               // retry if someone changed it meanwhile
        System.out.println("hand-written CAS loop -> " + hits.get());

        // AtomicReference: swap a whole immutable object atomically
        AtomicReference<Range> range = new AtomicReference<>(new Range(0, 10));
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) {
            ts[i] = new Thread(() -> {
                for (int k = 0; k < 10_000; k++)
                    range.updateAndGet(r -> new Range(r.low() + 1, r.high() + 1));   // function must be side-effect free
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("AtomicReference after 40,000 shifts -> " + range.get());

        // LongAdder: best for hot counters written by many threads, read rarely
        LongAdder requests = new LongAdder();
        Thread[] ws = new Thread[8];
        for (int i = 0; i < 8; i++) { ws[i] = new Thread(() -> { for (int k = 0; k < 100_000; k++) requests.increment(); }); ws[i].start(); }
        for (Thread t : ws) t.join();
        System.out.println("LongAdder sum -> " + requests.sum());
    }
}
