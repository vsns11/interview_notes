package ch16.gc;

import java.lang.management.*;
import java.util.SplittableRandom;

/**
 * A small allocation workload: a "cache" of 100,000 live byte arrays (~100 MB) that is constantly
 * replaced, like a server keeping recent sessions. Run it under different collectors, e.g.
 *   java -Xmx512m -XX:+UseG1GC -Xlog:gc -cp out ch16.gc.AllocWorkload
 *   java -Xmx512m -XX:+UseZGC  -Xlog:gc -cp out ch16.gc.AllocWorkload
 * A heartbeat thread measures the worst delay the application itself observed.
 */
public class AllocWorkload {
    static volatile boolean running = true;
    static volatile long worstStallMicros;

    public static void main(String[] args) throws Exception {
        long allocations = args.length > 0 ? Long.parseLong(args[0]) : 6_000_000;
        Thread heartbeat = Thread.ofPlatform().daemon().start(() -> {
            long last = System.nanoTime();
            while (running) {
                try { Thread.sleep(1); } catch (InterruptedException e) { return; }
                long now = System.nanoTime();
                long stall = (now - last) / 1_000 - 1_000;           // time beyond the 1 ms we asked for
                if (stall > worstStallMicros) worstStallMicros = stall;
                last = now;
            }
        });

        byte[][] cache = new byte[100_000][];                         // the live set
        SplittableRandom rnd = new SplittableRandom(42);              // fixed seed: same work every run
        long checksum = 0, bytes = 0, start = System.nanoTime();
        for (long i = 0; i < allocations; i++) {
            byte[] b = new byte[64 + rnd.nextInt(1_984)];            // 64..2047 bytes, avg ~1 KiB
            b[0] = (byte) i;
            bytes += b.length;
            int slot = rnd.nextInt(cache.length);
            if (cache[slot] != null) checksum += cache[slot][0];     // read the entry we evict
            cache[slot] = b;                                          // old entry becomes garbage
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        running = false;
        heartbeat.join();

        System.out.printf("allocated      : %,d MB of payload in %,d arrays (checksum %d)%n", bytes / 1_000_000, allocations, checksum);
        long cpuMs = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
                .getProcessCpuTime() / 1_000_000;                    // all threads, including GC threads
        System.out.printf("elapsed        : %,d ms wall, %,d ms CPU (whole process)%n", ms, cpuMs);
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans())
            System.out.printf("  %-22s count %4d, time %,5d ms%n", gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        System.out.printf("worst stall    : %.1f ms (seen by the heartbeat thread)%n", worstStallMicros / 1000.0);
    }
}
