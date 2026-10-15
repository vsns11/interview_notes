package ch22.rwlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class Main {

    /** Adapter so the JDK lock (fair mode) can run in the same experiments. */
    static final class JdkFairRWLock implements SimpleRWLock {
        private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock(true);
        public void lockRead() throws InterruptedException { rw.readLock().lockInterruptibly(); }
        public void unlockRead() { rw.readLock().unlock(); }
        public void lockWrite() throws InterruptedException { rw.writeLock().lockInterruptibly(); }
        public void unlockWrite() { rw.writeLock().unlock(); }
        public String name() { return "JDK ReentrantReadWriteLock(fair)"; }
    }

    public static void main(String[] args) throws Exception {
        List<Supplier<SimpleRWLock>> locks = List.of(
                ReaderPreferenceRWLock::new, WriterPreferenceRWLock::new, JdkFairRWLock::new);

        System.out.println("== 1. safety: 4 readers + 2 writers, 2000 operations each ==");
        for (Supplier<SimpleRWLock> s : locks) safety(s.get());

        System.out.println();
        System.out.println("== 2. writer arrives while 6 readers keep overlapping for 600 ms == [ms: nondeterministic]");
        for (Supplier<SimpleRWLock> s : locks) writerUnderReaderStream(s.get());

        System.out.println();
        System.out.println("== 3. reader arrives while 2 writers keep alternating (1 ms gap) for 600 ms == [ms: nondeterministic]");
        for (Supplier<SimpleRWLock> s : locks) readerUnderWriterStream(s.get());
    }

    // ---------------------------------------------------------------- 1. safety
    static void safety(SimpleRWLock lock) throws InterruptedException {
        AtomicInteger readersInside = new AtomicInteger();
        AtomicInteger writersInside = new AtomicInteger();
        AtomicInteger violations = new AtomicInteger();
        AtomicInteger maxReaders = new AtomicInteger();
        int[] pair = new int[2];                       // writers keep pair[0] == pair[1]
        List<Thread> threads = new ArrayList<>();
        for (int r = 0; r < 4; r++) {
            threads.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < 2_000; i++) {
                    try { lock.lockRead(); } catch (InterruptedException e) { return; }
                    try {
                        int n = readersInside.incrementAndGet();
                        maxReaders.accumulateAndGet(n, Math::max);
                        if (writersInside.get() != 0) violations.incrementAndGet();
                        if (pair[0] != pair[1]) violations.incrementAndGet();   // torn read
                        if (i % 100 == 0) sleep(1);                     // hold the read lock a while now and then
                        readersInside.decrementAndGet();
                    } finally {
                        lock.unlockRead();
                    }
                }
            }));
        }
        for (int w = 0; w < 2; w++) {
            threads.add(Thread.ofPlatform().start(() -> {
                for (int i = 0; i < 2_000; i++) {
                    try { lock.lockWrite(); } catch (InterruptedException e) { return; }
                    try {
                        if (writersInside.incrementAndGet() != 1 || readersInside.get() != 0) violations.incrementAndGet();
                        pair[0]++;
                        pair[1]++;
                        writersInside.decrementAndGet();
                    } finally {
                        lock.unlockWrite();
                    }
                }
            }));
        }
        for (Thread t : threads) t.join();
        System.out.printf("%-34s violations=%d  pair=(%d,%d)  readers overlapped (max>1)=%b%n",
                lock.name(), violations.get(), pair[0], pair[1], maxReaders.get() > 1);
    }

    // ------------------------------------------------- 2. can a writer starve?
    static void writerUnderReaderStream(SimpleRWLock lock) throws InterruptedException {
        long start = System.nanoTime();
        long stopAt = start + TimeUnit.MILLISECONDS.toNanos(600);
        List<Thread> readers = new ArrayList<>();
        for (int r = 0; r < 6; r++) {
            final int offset = r * 5;
            readers.add(Thread.ofPlatform().start(() -> {
                sleep(offset);                            // stagger so reads overlap
                while (System.nanoTime() < stopAt) {
                    try { lock.lockRead(); } catch (InterruptedException e) { return; }
                    try { sleep(30); } finally { lock.unlockRead(); }
                }
            }));
        }
        sleep(100);
        long waitMs = timeToAcquire(lock, true);
        for (Thread t : readers) t.join();
        System.out.printf("%-34s writer waited %3d ms, starved (>= 400 ms)? %b%n",
                lock.name(), waitMs, waitMs >= 400);
    }

    // ------------------------------------------------- 3. can a reader starve?
    static void readerUnderWriterStream(SimpleRWLock lock) throws InterruptedException {
        long stopAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(600);
        List<Thread> writers = new ArrayList<>();
        for (int w = 0; w < 2; w++) {
            final int offset = w * 10;
            writers.add(Thread.ofPlatform().start(() -> {
                sleep(offset);
                while (System.nanoTime() < stopAt) {
                    try { lock.lockWrite(); } catch (InterruptedException e) { return; }
                    try { sleep(20); } finally { lock.unlockWrite(); }
                    sleep(1);   // small gap: without it the releasing writer barges straight back in
                }
            }));
        }
        sleep(100);
        long waitMs = timeToAcquire(lock, false);
        for (Thread t : writers) t.join();
        System.out.printf("%-34s reader waited %3d ms, starved (>= 400 ms)? %b%n",
                lock.name(), waitMs, waitMs >= 400);
    }

    /** Runs one acquisition in a fresh thread and returns how long it waited. */
    static long timeToAcquire(SimpleRWLock lock, boolean write) throws InterruptedException {
        long[] waited = new long[1];
        CountDownLatch done = new CountDownLatch(1);
        Thread.ofPlatform().start(() -> {
            long t0 = System.nanoTime();
            try {
                if (write) lock.lockWrite(); else lock.lockRead();
            } catch (InterruptedException e) {
                return;
            }
            waited[0] = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            if (write) lock.unlockWrite(); else lock.unlockRead();
            done.countDown();
        });
        done.await();                                  // countDown/await = happens-before for waited[0]
        return waited[0];
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
