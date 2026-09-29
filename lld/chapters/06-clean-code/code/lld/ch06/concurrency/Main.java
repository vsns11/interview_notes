package lld.ch06.concurrency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Clean concurrency: separate concurrency from logic, limit shared data, prefer copies and independence, use the library. */
public class Main {

    static final List<String> PAGES = List.of(
            "the cat sat on the mat", "the dog sat", "a cat and a dog", "the end");

    /** Pure, single-threaded logic -- testable without any threads (SRP: concurrency kept separate). */
    static Map<String, Integer> countWords(String page) {
        Map<String, Integer> counts = new HashMap<>();
        for (String w : page.split(" ")) counts.merge(w, 1, Integer::sum);
        return counts;
    }

    // ---------------- BEFORE: threads share an unsynchronised counter ----------------
    static class Before {
        static int theCount = 0;                                      // shared mutable state, no synchronisation
        static int run(int repeats) throws InterruptedException {
            Thread[] threads = new Thread[4];
            for (int t = 0; t < threads.length; t++) {
                threads[t] = new Thread(() -> {
                    for (int r = 0; r < repeats; r++)
                        for (String page : PAGES)
                            for (String w : page.split(" "))
                                if (w.equals("the")) theCount++;      // read-modify-write race: updates get lost
                });
                threads[t].start();
            }
            for (Thread t : threads) t.join();
            return theCount;
        }
        // (Sharing a plain HashMap this way is even worse: concurrent resizes can corrupt it.)
    }

    // ---------------- AFTER ----------------
    static class After {
        /** Each task works on its OWN data and returns a result; results are combined once, at the end. */
        static Map<String, Integer> run(int repeats, int threads) throws Exception {
            ExecutorService pool = Executors.newFixedThreadPool(threads);   // know your library
            try {
                List<Callable<Map<String, Integer>>> tasks = new java.util.ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    tasks.add(() -> {
                        Map<String, Integer> local = new HashMap<>();       // thread-confined: no sharing, no locks
                        for (int r = 0; r < repeats; r++)
                            for (String page : PAGES) countWords(page).forEach((w, c) -> local.merge(w, c, Integer::sum));
                        return local;
                    });
                }
                Map<String, Integer> total = new HashMap<>();              // combined by ONE thread: no sharing
                for (Future<Map<String, Integer>> f : pool.invokeAll(tasks))
                    f.get().forEach((w, c) -> total.merge(w, c, Integer::sum));   // Future.get() safely hands over each result
                return Map.copyOf(total);                                           // publish an immutable copy
            } finally {
                pool.shutdown();                                                    // clean shutdown is part of the design
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) pool.shutdownNow();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int repeats = 20_000;
        int expectedThe = 4 * repeats * 4;   // "the" appears 4 times per pass over PAGES, 4 threads
        System.out.println("pure logic: " + new java.util.TreeMap<>(countWords("the cat sat on the mat")));

        System.out.println("== BEFORE (racy; the number varies from run to run) ==");
        int racy = Before.run(repeats);
        System.out.println("count(the) = " + racy + " expected " + expectedThe + " -> lost " + (expectedThe - racy) + " updates");

        System.out.println("== AFTER (deterministic) ==");
        for (int threads : new int[]{1, 4, 16}) {                    // more threads than cores is a good stress test
            Map<String, Integer> safe = After.run(repeats, threads);
            System.out.println(threads + " thread(s): count(the) = " + safe.get("the") + " expected " + (4 * repeats * threads));
        }
    }
}
