package ch22.unisexbathroom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ch22.unisexbathroom.Bathroom.Gender;

public class Main {

    interface Body { void run() throws InterruptedException; }

    static Thread spawn(String name, Body body) {
        Thread t = new Thread(() -> {
            try { body.run(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, name);
        t.start();
        return t;
    }

    /** 6 men and 6 women, 5 visits each, capacity 3. Checks the two safety rules. */
    static void stress(boolean fair) throws InterruptedException {
        Bathroom room = new Bathroom(3, fair);
        AtomicInteger visits = new AtomicInteger();
        List<Thread> people = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Gender g = i % 2 == 0 ? Gender.MAN : Gender.WOMAN;
            people.add(spawn(g + "-" + i, () -> {
                for (int v = 0; v < 5; v++) {
                    room.enter(g);
                    Thread.sleep(5);                   // "using the bathroom"
                    visits.incrementAndGet();
                    room.leave(g);
                }
            }));
        }
        for (Thread t : people) t.join();
        System.out.printf("stress (fair=%b): visits=%d, max inside=%d, mixed-gender moments=%d%n",
                fair, visits.get(), room.maxInside(), room.mixedViolations());
    }

    /**
     * 5 men keep using the room (40 ms each) for one second, starting 10 ms apart, so it is never empty.
     * One woman arrives at 100 ms. How long does she wait?
     */
    static long womanWaitMs(boolean fair) throws InterruptedException {
        Bathroom room = new Bathroom(3, fair);
        long end = System.nanoTime() + 1_000_000_000L;
        List<Thread> men = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int delay = i * 10;
            men.add(spawn("man-" + i, () -> {
                Thread.sleep(delay);
                while (System.nanoTime() < end) {
                    room.enter(Gender.MAN);
                    Thread.sleep(40);
                    room.leave(Gender.MAN);
                }
            }));
        }
        long[] waited = new long[1];
        Thread woman = spawn("woman", () -> {
            Thread.sleep(100);
            long t0 = System.nanoTime();
            room.enter(Gender.WOMAN);
            waited[0] = (System.nanoTime() - t0) / 1_000_000;
            room.leave(Gender.WOMAN);
        });
        woman.join();
        for (Thread t : men) t.join();
        return waited[0];
    }

    public static void main(String[] args) throws InterruptedException {
        stress(false);
        stress(true);
        long unfair = womanWaitMs(false);
        long fair = womanWaitMs(true);
        System.out.println("starvation test, men stream for 1000 ms, woman arrives at 100 ms:");
        System.out.println("  unfair: woman waited until the men stopped (>= 800 ms): " + (unfair >= 800));
        System.out.println("  fair:   woman got in after one batch (<= 100 ms):      " + (fair <= 100));
        System.out.println("  [nondeterministic] waits: unfair=" + unfair + " ms, fair=" + fair + " ms");
    }
}
