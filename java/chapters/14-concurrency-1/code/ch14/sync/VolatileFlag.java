package ch14.sync;

/** volatile guarantees that a write by one thread becomes visible to others (but not atomicity of ++). */
public class VolatileFlag {
    private static volatile boolean running = true;          // remove 'volatile' and the worker MAY spin forever
    private static volatile int volatileCounter = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            long spins = 0;
            while (running) spins++;                          // reads the flag each iteration
            System.out.println("worker saw running=false and stopped (spins > 0: " + (spins > 0) + ")");
        });
        worker.start();
        Thread.sleep(200);
        running = false;                                      // visible to the worker because of volatile
        worker.join();

        // volatile does NOT make count++ atomic
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) {
            ts[i] = new Thread(() -> { for (int k = 0; k < 1_000_000; k++) volatileCounter++; });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.printf("volatile count++ from 4 threads: %,d of 4,000,000   [nondeterministic - still loses updates]%n", volatileCounter);
    }
}
