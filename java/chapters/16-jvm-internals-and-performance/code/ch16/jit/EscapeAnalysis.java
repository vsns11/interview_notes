package ch16.jit;

import java.lang.management.ManagementFactory;

/**
 * Escape analysis: C2 can remove an allocation when the object never leaves the method
 * ("scalar replacement"). Compare:  java -cp out ch16.jit.EscapeAnalysis
 *                            with:  java -XX:-DoEscapeAnalysis -cp out ch16.jit.EscapeAnalysis
 */
public class EscapeAnalysis {
    record Point(int x, int y) {
        Point plus(Point o) { return new Point(x + o.x, y + o.y); }
    }

    static Point escaped;                                    // writing here makes an object escape

    /** Points are created and dropped inside the method: they do NOT escape. */
    static long sumLocal(int n) {
        long total = 0;
        for (int i = 0; i < n; i++) {
            Point p = new Point(i, i).plus(new Point(1, 2)); // 3 Point allocations per iteration... on paper
            total += p.x() + p.y();
        }
        return total;
    }

    /** Same work, but one Point is published to a static field every iteration: it escapes. */
    static long sumEscaping(int n) {
        long total = 0;
        for (int i = 0; i < n; i++) {
            Point p = new Point(i, i).plus(new Point(1, 2));
            escaped = p;
            total += p.x() + p.y();
        }
        return total;
    }

    static final com.sun.management.ThreadMXBean T =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    interface Work { long run(int n); }

    static void measure(String label, Work w) {
        for (int i = 0; i < 20; i++) w.run(100_000);         // warm-up: let C2 compile the method
        long before = T.getCurrentThreadAllocatedBytes();
        long r = w.run(10_000_000);
        long bytes = T.getCurrentThreadAllocatedBytes() - before;
        System.out.printf("%-10s result=%d  heap allocated for 10M iterations: %,d bytes%n", label, r, bytes);
    }

    public static void main(String[] args) {
        System.out.println("DoEscapeAnalysis = " + !ManagementFactory.getRuntimeMXBean()
                .getInputArguments().contains("-XX:-DoEscapeAnalysis"));
        measure("local", EscapeAnalysis::sumLocal);
        measure("escaping", EscapeAnalysis::sumEscaping);
    }
}
