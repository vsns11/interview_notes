package ch14.atomic;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.*;

/** The rest of java.util.concurrent.atomic: booleans, arrays, stamped/markable refs, field updaters, accumulators. */
public class AtomicFamily {

    /** Runs `body` on n threads at once and waits for all of them. */
    static void onThreads(int n, Runnable body) throws InterruptedException {
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; i++) (ts[i] = new Thread(body)).start();
        for (Thread t : ts) t.join();
    }

    // AtomicBoolean: run an action exactly once
    static final AtomicBoolean started = new AtomicBoolean(false);
    static final AtomicInteger initRuns = new AtomicInteger();
    static void initOnce() { if (started.compareAndSet(false, true)) initRuns.incrementAndGet(); }

    // Field updaters: atomic ops on an ordinary volatile field (no extra object per instance)
    static class Connection {
        volatile int refCount;                    // must be volatile, non-static, and an int (not Integer)
        volatile String state = "NEW";
        int notVolatile;
    }
    static final AtomicIntegerFieldUpdater<Connection> REFS =
            AtomicIntegerFieldUpdater.newUpdater(Connection.class, "refCount");
    static final AtomicReferenceFieldUpdater<Connection, String> STATE =
            AtomicReferenceFieldUpdater.newUpdater(Connection.class, String.class, "state");
    static final VarHandle REFS_VH;               // the Java 9+ low-level equivalent
    static {
        try { REFS_VH = MethodHandles.lookup().findVarHandle(Connection.class, "refCount", int.class); }
        catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }

    public static void main(String[] args) throws InterruptedException {
        onThreads(8, AtomicFamily::initOnce);
        System.out.println("AtomicBoolean       : 8 racing threads, init ran " + initRuns + " time(s)");

        AtomicIntegerArray histogram = new AtomicIntegerArray(4);       // each slot is its own atomic int
        onThreads(4, () -> { for (int i = 0; i < 10_000; i++) histogram.incrementAndGet(i % 4); });
        System.out.println("AtomicIntegerArray  : " + histogram);

        AtomicReferenceArray<String> seats = new AtomicReferenceArray<>(3);
        AtomicInteger winners = new AtomicInteger();
        onThreads(5, () -> { if (seats.compareAndSet(0, null, Thread.currentThread().getName())) winners.incrementAndGet(); });
        System.out.println("AtomicReferenceArray: 5 threads tried to claim seat 0, winners = " + winners);

        // ABA: A -> B -> A looks unchanged to a plain CAS, but the stamp moved on
        AtomicReference<String> plain = new AtomicReference<>("A");
        AtomicStampedReference<String> stamped = new AtomicStampedReference<>("A", 0);
        int seenStamp = stamped.getStamp();                            // a slow thread reads A, stamp 0 ...
        plain.set("B"); plain.set("A");                                // ... meanwhile others change A->B->A
        stamped.compareAndSet("A", "B", 0, 1); stamped.compareAndSet("B", "A", 1, 2);
        System.out.println("ABA                 : plain CAS A->C " + plain.compareAndSet("A", "C")
                + ", stamped CAS A->C " + stamped.compareAndSet("A", "C", seenStamp, seenStamp + 1)
                + " (stamp is now " + stamped.getStamp() + ")");

        // Markable: a reference plus one boolean, e.g. "this node is logically deleted"
        String next = "node-2";
        AtomicMarkableReference<String> link = new AtomicMarkableReference<>(next, false);
        boolean marked = link.attemptMark(next, true);                 // delete step 1: mark
        boolean inserted = link.compareAndSet(next, "node-new", false, false);  // insert expects unmarked
        System.out.println("AtomicMarkableRef   : marked deleted " + marked + ", later insert after it succeeds? " + inserted);

        Connection c = new Connection();
        onThreads(4, () -> { for (int i = 0; i < 10_000; i++) REFS.incrementAndGet(c); });
        System.out.println("IntegerFieldUpdater : refCount = " + c.refCount);
        System.out.println("RefFieldUpdater     : NEW->OPEN " + STATE.compareAndSet(c, "NEW", "OPEN")
                + ", NEW->OPEN again " + STATE.compareAndSet(c, "NEW", "OPEN"));
        System.out.println("VarHandle CAS       : 40000->0 " + REFS_VH.compareAndSet(c, 40_000, 0) + ", refCount = " + c.refCount);
        try {
            AtomicIntegerFieldUpdater.newUpdater(Connection.class, "notVolatile");
        } catch (IllegalArgumentException e) {
            System.out.println("updater on non-volatile field -> IllegalArgumentException: " + e.getMessage());
        }

        LongAccumulator maxLatency = new LongAccumulator(Long::max, Long.MIN_VALUE);   // (op, identity)
        AtomicInteger next2 = new AtomicInteger();
        onThreads(4, () -> { for (int i = 0; i < 25_000; i++) maxLatency.accumulate(next2.getAndIncrement()); });
        System.out.println("LongAccumulator(max): " + maxLatency.get());

        DoubleAdder revenue = new DoubleAdder();
        DoubleAccumulator cheapest = new DoubleAccumulator(Math::min, Double.POSITIVE_INFINITY);
        onThreads(4, () -> { for (int i = 1; i <= 1_000; i++) { revenue.add(0.25); cheapest.accumulate(i * 0.5); } });
        System.out.println("DoubleAdder sum     : " + revenue.sum() + ", DoubleAccumulator(min): " + cheapest.get());
    }
}
