package ch14.synchronizers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** Exchanger as a double buffer; Phaser with dynamic parties, onAdvance and termination. */
public class ExchangerPhaser {

    public static void main(String[] args) throws Exception {
        // ---- Exchanger: filler and drainer swap a full buffer for an empty one ----
        Exchanger<List<Integer>> exchanger = new Exchanger<>();
        List<String> log = new CopyOnWriteArrayList<>();
        Thread filler = new Thread(() -> {
            List<Integer> buf = new ArrayList<>();
            try {
                for (int i = 1; i <= 6; i++) {
                    buf.add(i);
                    if (buf.size() == 3) buf = exchanger.exchange(buf);   // hand over full, get back empty
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread drainer = new Thread(() -> {
            List<Integer> buf = new ArrayList<>();
            try {
                for (int round = 0; round < 2; round++) {
                    buf = exchanger.exchange(buf);                       // hand over empty, get back full
                    log.add("drained " + buf);
                    buf.clear();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        filler.start(); drainer.start(); filler.join(); drainer.join();
        System.out.println("Exchanger: " + log);
        try {
            exchanger.exchange(List.of(), 100, TimeUnit.MILLISECONDS);   // no partner arrives
        } catch (TimeoutException e) {
            System.out.println("Exchanger: no partner within 100 ms -> TimeoutException");
        }

        // ---- Phaser: workers leave at different times; onAdvance decides when to stop ----
        Phaser phaser = new Phaser(1) {                                 // 1 = main, the coordinator
            @Override protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("  phase " + phase + " done, parties still registered = " + registeredParties);
                return phase >= 3 || registeredParties == 0;            // true = terminate the phaser
            }
        };
        int[] roundsPerWorker = {1, 2, 4};                              // each worker needs a different number of phases
        for (int w = 0; w < roundsPerWorker.length; w++) {
            int rounds = roundsPerWorker[w];
            phaser.register();                                          // dynamic registration
            new Thread(() -> {
                for (int r = 0; r < rounds && !phaser.isTerminated(); r++) {
                    if (r == rounds - 1) phaser.arriveAndDeregister();  // last round: leave
                    else phaser.arriveAndAwaitAdvance();
                }
            }).start();
        }
        while (!phaser.isTerminated()) phaser.arriveAndAwaitAdvance();  // main drives the phases
        System.out.println("Phaser: terminated = " + phaser.isTerminated() + ", getPhase() = " + phaser.getPhase()
                + " (negative once terminated)");
        System.out.println("Phaser: awaitAdvance on a terminated phaser returns at once: " + phaser.awaitAdvance(0));
    }
}
