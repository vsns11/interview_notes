package ch22.orderedprinting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Runs every Printer against all 6 possible arrival orders of the three threads.
 * Threads are started 20 ms apart in the given order, so the "wrong" thread really does arrive first.
 */
public class Main {

    interface Call { void call(OrderedPrinters.Printer p, StringBuffer out) throws InterruptedException; }

    static final Call FIRST  = (p, out) -> p.first(() -> out.append("first "));
    static final Call SECOND = (p, out) -> p.second(() -> out.append("second "));
    static final Call THIRD  = (p, out) -> p.third(() -> out.append("third "));

    static final int[][] ORDERS = {{1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 1, 2}, {3, 2, 1}};

    /** Starts the three calls in the given arrival order and returns what was printed. */
    static String run(OrderedPrinters.Printer printer, int[] arrival) throws InterruptedException {
        StringBuffer out = new StringBuffer();          // StringBuffer: its methods are synchronized
        Call[] calls = {null, FIRST, SECOND, THIRD};
        List<Thread> threads = new ArrayList<>();
        for (int which : arrival) {
            Call c = calls[which];
            Thread t = new Thread(() -> {
                try { c.call(printer, out); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "caller-" + which);
            threads.add(t);
            t.start();
            Thread.sleep(20);                            // make the arrival order real
        }
        for (Thread t : threads) t.join();               // join: all appends are visible to main afterwards
        return out.toString().trim();
    }

    static void check(String name, Supplier<OrderedPrinters.Printer> factory) throws InterruptedException {
        int ok = 0;
        for (int[] order : ORDERS) {
            if (run(factory.get(), order).equals("first second third")) ok++;   // fresh object per run
        }
        System.out.printf("%-17s %d/6 arrival orders printed 'first second third'%n", name, ok);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("-- broken version: prints in arrival order --");
        for (int[] order : new int[][]{{3, 2, 1}, {2, 3, 1}}) {
            System.out.printf("arrival %d,%d,%d -> %s%n", order[0], order[1], order[2],
                    run(new OrderedPrinters.Unsynchronized(), order));
        }
        System.out.println("-- correct versions, all 6 arrival orders each --");
        check("CountDownLatch", OrderedPrinters.LatchPrinter::new);
        check("Semaphore(0)", OrderedPrinters.SemaphorePrinter::new);
        check("wait/notifyAll", OrderedPrinters.MonitorPrinter::new);
        check("volatile spin", OrderedPrinters.SpinPrinter::new);
    }
}
