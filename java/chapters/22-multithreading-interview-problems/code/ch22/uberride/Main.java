package ch22.uberride;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import ch22.uberride.UberSeating.Party;

public class Main {

    /** Records who sat in the current car and checks every rule. */
    static final class Recorder implements UberSeating.Car {
        final boolean verbose;
        final List<String> seats = new ArrayList<>();   // guarded by 'this'
        final AtomicInteger cars = new AtomicInteger(), illegal = new AtomicInteger(), drives = new AtomicInteger();
        Recorder(boolean verbose) { this.verbose = verbose; }

        public synchronized void seated(Party p, String rider) { seats.add(rider); }

        /** Barrier action: runs once per car, after the 4th seated() and before anyone continues. */
        synchronized void carFull() {
            long blue = seats.stream().filter(s -> s.startsWith("B")).count();
            if (seats.size() != 4 || blue == 1 || blue == 3) illegal.incrementAndGet();
            cars.incrementAndGet();
            Collections.sort(seats);
            if (verbose) System.out.println("car " + cars.get() + " seated: " + seats);
            seats.clear();
        }

        public void drive(String captain) {
            drives.incrementAndGet();
            if (verbose) System.out.println("      driven by " + captain);
        }
    }

    static Thread rider(UberSeating u, Party p, String name) {
        Thread t = new Thread(() -> {
            try { u.ride(p, name); } catch (Exception e) { Thread.currentThread().interrupt(); }
        }, name);
        t.start();
        return t;
    }

    public static void main(String[] args) throws InterruptedException {
        // 1) Scripted arrivals, 40 ms apart, so the cars that form are predictable.
        Recorder rec = new Recorder(true);
        UberSeating uber = new UberSeating(rec, rec::carFull);
        String script = "B1 B2 B3 R1 R2 R3 R4 R5 B4 R6 R7 R8";
        List<Thread> all = new ArrayList<>();
        System.out.println("arrival order: " + script);
        for (String name : script.split(" ")) {
            all.add(rider(uber, name.startsWith("B") ? Party.BLUE : Party.RED, name));
            Thread.sleep(40);
        }
        for (Thread t : all) t.join();

        // 2) Stress: 20 BLUE + 20 RED in random order, no pauses.
        Recorder stress = new Recorder(false);
        UberSeating u2 = new UberSeating(stress, stress::carFull);
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= 20; i++) { names.add("B" + i); names.add("R" + i); }
        Collections.shuffle(names, new Random(7));
        all.clear();
        for (String n : names) all.add(rider(u2, n.startsWith("B") ? Party.BLUE : Party.RED, n));
        for (Thread t : all) t.join();
        System.out.printf("stress: 40 riders -> cars=%d, drive() calls=%d, illegal 3+1 cars=%d%n",
                stress.cars.get(), stress.drives.get(), stress.illegal.get());
    }
}
