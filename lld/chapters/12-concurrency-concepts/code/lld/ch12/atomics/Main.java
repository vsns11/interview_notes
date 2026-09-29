package lld.ch12.atomics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.LongAdder;

/** Compare-and-swap (CAS), a lock-free stack, LongAdder, and the ABA problem. */
public class Main {

    /** Increment written by hand as a CAS retry loop (this is what incrementAndGet does inside). */
    static int casIncrement(AtomicInteger a) {
        while (true) {
            int current = a.get();                    // 1. read
            int next = current + 1;                   // 2. compute
            if (a.compareAndSet(current, next)) {     // 3. write only if nobody changed it meanwhile
                return next;
            }                                         // 4. someone won the race: retry
        }
    }

    /** Treiber stack: a classic lock-free data structure built on one AtomicReference. */
    static class LockFreeStack<T> {
        private record Node<T>(T value, Node<T> next) { }
        private final AtomicReference<Node<T>> head = new AtomicReference<>();

        void push(T v) {
            Node<T> oldHead, newHead;
            do {
                oldHead = head.get();
                newHead = new Node<>(v, oldHead);
            } while (!head.compareAndSet(oldHead, newHead));
        }
        T pop() {
            Node<T> oldHead;
            do {
                oldHead = head.get();
                if (oldHead == null) return null;
            } while (!head.compareAndSet(oldHead, oldHead.next()));
            return oldHead.value();
        }
    }

    static void runThreads(int n, Runnable r) throws InterruptedException {
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < n; i++) { Thread t = new Thread(r); ts.add(t); t.start(); }
        for (Thread t : ts) t.join();
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger counter = new AtomicInteger();
        runThreads(4, () -> { for (int i = 0; i < 100_000; i++) casIncrement(counter); });
        System.out.println("hand-written CAS loop counter = " + counter.get());

        AtomicInteger highWater = new AtomicInteger();
        runThreads(4, () -> {
            for (int i = 0; i < 1_000; i++) highWater.accumulateAndGet(i, Math::max);  // atomic "max"
        });
        System.out.println("atomic max                    = " + highWater.get());

        LockFreeStack<Integer> stack = new LockFreeStack<>();
        runThreads(4, () -> { for (int i = 0; i < 10_000; i++) stack.push(i); });
        int popped = 0;
        while (stack.pop() != null) popped++;
        System.out.println("lock-free stack popped        = " + popped);

        LongAdder hits = new LongAdder();              // striped counter: fast under heavy contention
        runThreads(8, () -> { for (int i = 0; i < 100_000; i++) hits.increment(); });
        System.out.println("LongAdder sum                 = " + hits.sum());

        // ABA: value goes A -> B -> A; a plain CAS cannot tell anything happened.
        AtomicReference<String> ref = new AtomicReference<>("A");
        String seen = ref.get();
        ref.set("B"); ref.set("A");                     // another thread's changes
        System.out.println("plain CAS after A->B->A succeeds   : " + ref.compareAndSet(seen, "C"));

        AtomicStampedReference<String> sref = new AtomicStampedReference<>("A", 0);
        int[] stampHolder = new int[1];
        String seen2 = sref.get(stampHolder);
        int seenStamp = stampHolder[0];
        sref.set("B", seenStamp + 1); sref.set("A", seenStamp + 2);
        System.out.println("stamped CAS after A->B->A succeeds : " + sref.compareAndSet(seen2, "C", seenStamp, seenStamp + 1));
    }
}
