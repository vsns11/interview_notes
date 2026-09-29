package lld.ch08.objectpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    /** Stand-in for an expensive resource such as a DB connection. */
    static final class FakeConnection {
        private static final AtomicInteger IDS = new AtomicInteger();
        final int id = IDS.incrementAndGet();
        int queriesRun = 0;
        boolean broken = false;
        String query(String sql) { queriesRun++; return "conn#" + id + " ran: " + sql; }
    }

    public static void main(String[] args) throws Exception {
        ObjectPool<FakeConnection> pool = new ObjectPool<>(
                3,
                () -> { System.out.println("  (opening a new connection - expensive)"); return new FakeConnection(); },
                c -> c.queriesRun = 0,              // reset per-borrower state
                c -> !c.broken);                    // validate on return

        System.out.println("== 10 tasks on 6 threads share at most 3 connections ==");
        List<Integer> usedIds = new CopyOnWriteArrayList<>();
        try (ExecutorService workers = Executors.newFixedThreadPool(6)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                final int task = i;
                futures.add(workers.submit(() -> {
                    FakeConnection c = pool.acquire(2, TimeUnit.SECONDS);
                    try {                                       // ALWAYS release in finally
                        c.query("SELECT " + task);
                        usedIds.add(c.id);
                        Thread.sleep(20);
                    } finally {
                        pool.release(c);
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("  tasks finished: " + usedIds.size()
                + ", connections ever created: " + pool.created()
                + ", distinct connections used: " + usedIds.stream().distinct().count());

        System.out.println("\n== A broken connection is discarded, not reused ==");
        FakeConnection c = pool.acquire(1, TimeUnit.SECONDS);
        c.broken = true;
        pool.release(c);
        System.out.println("  created after discarding: " + pool.created() + ", idle: " + pool.idle());

        System.out.println("\n== Exhaustion: borrowers wait, then time out ==");
        List<FakeConnection> held = new ArrayList<>();
        for (int i = 0; i < 3; i++) held.add(pool.acquire(1, TimeUnit.SECONDS));
        try {
            pool.acquire(100, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            System.out.println("  " + e.getMessage());
        }
        held.forEach(pool::release);
        System.out.println("  after returning all: idle = " + pool.idle());
    }
}
