package ch14.threads;

/** Four ways to define a thread's work, start vs run, join, names and daemon threads. */
public class CreatingThreads {

    static class Greeter extends Thread {                       // 1) subclass Thread (least flexible)
        Greeter() { super("greeter"); }
        @Override public void run() { System.out.println("1 subclass  running in " + Thread.currentThread().getName()); }
    }

    static class Task implements Runnable {                     // 2) implement Runnable (separates task from thread)
        @Override public void run() { System.out.println("2 Runnable  running in " + Thread.currentThread().getName()); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("main thread is called '" + Thread.currentThread().getName() + "'");

        Thread t1 = new Greeter();
        Thread t2 = new Thread(new Task(), "worker-2");
        Thread t3 = new Thread(() -> System.out.println("3 lambda    running in " + Thread.currentThread().getName()), "worker-3");
        Thread t4 = Thread.ofPlatform().name("worker-4").unstarted(   // 4) Thread.Builder (Java 21)
                () -> System.out.println("4 builder   running in " + Thread.currentThread().getName()));

        // start() creates a new thread; join() waits for it. Started one at a time here so the order is fixed.
        for (Thread t : new Thread[]{t1, t2, t3, t4}) { t.start(); t.join(); }

        // run() is just a method call: no new thread!
        Thread notStarted = new Thread(() -> System.out.println("run() called directly executes in " + Thread.currentThread().getName()));
        notStarted.run();
        System.out.println("state after run(): " + notStarted.getState());

        try {
            t1.start();                                          // a thread can be started only once
        } catch (IllegalThreadStateException e) {
            System.out.println("start() twice -> IllegalThreadStateException");
        }

        // Concurrent start: output order between these lines is NOT fixed
        Thread a = new Thread(() -> System.out.println("   [order varies] A says hi"));
        Thread b = new Thread(() -> System.out.println("   [order varies] B says hi"));
        a.start(); b.start();
        a.join(); b.join();                                       // main waits for both
        System.out.println("both finished (join guarantees this line comes last)");

        // Daemon threads don't keep the JVM alive
        Thread daemon = new Thread(() -> { while (true) Thread.onSpinWait(); }, "background-spinner");
        daemon.setDaemon(true);                                  // must be set before start()
        daemon.start();
        System.out.println("daemon? " + daemon.isDaemon() + ", alive? " + daemon.isAlive()
                + ", id>0? " + (daemon.threadId() > 0));        // threadId(): Java 19
        System.out.println("main ends; the JVM exits even though the daemon still spins");
    }
}
