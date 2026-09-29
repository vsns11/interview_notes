package ch16.layout;

import java.lang.management.ManagementFactory;

/**
 * Measures the real heap cost of common objects by asking the JVM how many bytes
 * this thread allocated (com.sun.management.ThreadMXBean). No agent or library needed.
 */
public class ObjectSizes {
    record Point(int x, int y) {}
    static class Node { Object value; Node next; }

    static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static final int N = 1_000_000;
    static Object[] sink = new Object[N];             // keeps objects reachable so none can be optimised away

    interface Maker { Object make(int i); }

    static double bytesPerObject(Maker m) {
        for (int i = 0; i < N; i++) sink[i] = m.make(i);          // warm-up round
        long before = THREADS.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < N; i++) sink[i] = m.make(i);
        long after = THREADS.getCurrentThreadAllocatedBytes();
        return (after - before) / (double) N;
    }

    public static void main(String[] args) {
        System.out.println("compact object headers: " +
                ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-XX:+UseCompactObjectHeaders"));
        System.out.printf("%-28s %6.1f bytes%n", "new Object()", bytesPerObject(i -> new Object()));
        System.out.printf("%-28s %6.1f bytes%n", "Point(int x, int y)", bytesPerObject(i -> new Point(i, i)));
        System.out.printf("%-28s %6.1f bytes%n", "Node{Object, Node}", bytesPerObject(i -> new Node()));
        System.out.printf("%-28s %6.1f bytes%n", "Long (value > 127)", bytesPerObject(i -> Long.valueOf(i + 1000L)));
        System.out.printf("%-28s %6.1f bytes%n", "new int[0]", bytesPerObject(i -> new int[0]));
        System.out.printf("%-28s %6.1f bytes%n", "new int[10]", bytesPerObject(i -> new int[10]));
        System.out.printf("%-28s %6.1f bytes%n", "new byte[10]", bytesPerObject(i -> new byte[10]));
        System.out.printf("%-28s %6.1f bytes%n", "\"s\" + i (String, ~6 chars)",
                bytesPerObject(i -> "s" + (100_000 + i % 100_000)));
    }
}
