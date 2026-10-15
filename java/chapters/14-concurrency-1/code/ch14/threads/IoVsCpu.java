package ch14.threads;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** I/O-bound vs CPU-bound: how busy is the CPU, and what do extra threads buy? */
public class IoVsCpu {
    static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();

    static void ioTask() {                                       // stands in for a 100 ms network call
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
    static volatile long sink;
    static void cpuTask() {                                      // fixed amount of arithmetic
        long x = 0;
        for (long i = 0; i < 100_000_000L; i++) x += i ^ (x >>> 3);
        sink = x;
    }

    public static void main(String[] args) throws Exception {
        profile("I/O-bound task", IoVsCpu::ioTask);
        profile("CPU-bound task", IoVsCpu::cpuTask);

        int cores = Runtime.getRuntime().availableProcessors();
        int tasks = cores * 4;
        System.out.println("cores = " + cores + ", tasks = " + tasks + ", pool = " + tasks + " threads   [machine-dependent]");
        System.out.println("I/O tasks: speed-up vs one thread = " + speedup(tasks, IoVsCpu::ioTask) + "x   [machine-dependent]");
        System.out.println("CPU tasks: speed-up vs one thread = " + speedup(tasks, IoVsCpu::cpuTask) + "x   [machine-dependent]");
    }

    /** Wall-clock time vs CPU time actually used by this thread. */
    static void profile(String name, Runnable task) {
        long wall0 = System.nanoTime(), cpu0 = MX.getCurrentThreadCpuTime();
        task.run();
        long wallMs = (System.nanoTime() - wall0) / 1_000_000;
        long cpuMs = (MX.getCurrentThreadCpuTime() - cpu0) / 1_000_000;
        System.out.printf("%s: wall %d ms, CPU %d ms -> CPU busy %d%%   [machine-dependent]%n",
                name, wallMs, cpuMs, Math.round(100.0 * cpuMs / Math.max(1, wallMs)));
    }

    /** Runs the tasks on 1 thread, then on `tasks` threads; returns time(1) / time(many). */
    static long speedup(int tasks, Runnable task) throws Exception {
        return Math.round((double) timeOn(1, tasks, task) / timeOn(tasks, tasks, task));
    }
    static long timeOn(int threads, int tasks, Runnable task) throws Exception {
        long t0 = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> fs = new ArrayList<>();
            for (int i = 0; i < tasks; i++) fs.add(pool.submit(task));
            for (Future<?> f : fs) f.get();
        }
        return System.nanoTime() - t0;
    }
}
