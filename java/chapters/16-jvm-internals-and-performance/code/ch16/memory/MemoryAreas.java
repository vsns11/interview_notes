package ch16.memory;

import java.lang.management.*;

/** Lists the JVM's memory pools and measures how deep the thread stack lets us recurse. */
public class MemoryAreas {
    static int depth;

    static void recurse() { depth++; recurse(); }        // each call pushes one stack frame

    public static void main(String[] args) throws Exception {
        final long MiB = 1024 * 1024;
        System.out.println("Garbage collector(s): ");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans())
            System.out.println("  " + gc.getName());

        System.out.println("Memory pools (type, used KiB / max MiB):");
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage u = p.getUsage();
            String max = u.getMax() < 0 ? "no limit" : String.valueOf(u.getMax() / MiB);
            System.out.printf("  %-8s %-34s %7d / %s%n",
                    p.getType() == MemoryType.HEAP ? "HEAP" : "NON-HEAP", p.getName(), u.getUsed() / 1024, max);
        }

        // Stack: run recursion in threads with different stack sizes (a hint the JVM honours on most platforms)
        for (long stackSize : new long[] {256 * 1024, 1024 * 1024, 8 * 1024 * 1024}) {
            depth = 0;
            Thread t = new Thread(null, () -> {
                try { recurse(); } catch (StackOverflowError e) { /* expected */ }
            }, "deep", stackSize);
            t.start();
            t.join();
            System.out.printf("Stack %5d KiB -> StackOverflowError after ~%,d frames%n", stackSize / 1024, depth);
        }
    }
}
