package lld.ch13.h2o;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * LeetCode 1117 - Building H2O.
 * Hydrogen and oxygen threads arrive in any order. They must leave in groups of three:
 * exactly 2 H + 1 O per group, and a group must fully leave before the next one starts.
 */
public class Main {

    static class H2O {
        private final Semaphore hSlots = new Semaphore(2);    // at most 2 H in the current molecule
        private final Semaphore oSlots = new Semaphore(1);    // at most 1 O in the current molecule
        private final CyclicBarrier bond = new CyclicBarrier(3);   // wait until all 3 atoms are present

        void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            hSlots.acquire();
            try {
                bond.await();
            } catch (BrokenBarrierException e) { throw new IllegalStateException(e); }
            releaseHydrogen.run();
            hSlots.release();                                 // free my slot only after I have "left"
        }
        void oxygen(Runnable releaseOxygen) throws InterruptedException {
            oSlots.acquire();
            try {
                bond.await();
            } catch (BrokenBarrierException e) { throw new IllegalStateException(e); }
            releaseOxygen.run();
            oSlots.release();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 16 H + 8 O = 8 molecules, arriving in a scrambled (but reproducible) order.
        List<Character> atoms = new ArrayList<>();
        for (int i = 0; i < 8; i++) { atoms.add('H'); atoms.add('H'); atoms.add('O'); }
        java.util.Collections.shuffle(atoms, new java.util.Random(7));
        StringBuilder in = new StringBuilder();
        atoms.forEach(in::append);
        String input = in.toString();
        System.out.println("arrival order   : " + input);
        StringBuffer out = new StringBuffer();
        H2O h2o = new H2O();
        List<Thread> ts = new ArrayList<>();
        for (char c : input.toCharArray()) {
            Thread t = new Thread(() -> {
                try {
                    if (c == 'H') h2o.hydrogen(() -> out.append('H'));
                    else h2o.oxygen(() -> out.append('O'));
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            ts.add(t);
            t.start();
        }
        for (Thread t : ts) t.join();

        // Output order varies run to run (HHO, HOH, OHH are all fine), so we verify it instead.
        String s = out.toString();
        boolean valid = s.length() == input.length();
        for (int i = 0; valid && i < s.length(); i += 3) {
            String g = s.substring(i, i + 3);
            valid = g.chars().filter(ch -> ch == 'H').count() == 2 && g.indexOf('O') >= 0;
        }
        System.out.println("molecules formed: " + s.length() / 3 + ", every group of 3 has 2 H + 1 O: " + valid);
    }
}
