package ch14.collections;

import java.util.*;
import java.util.concurrent.*;

/** Thread-safe collections and the compound-action trap. */
public class ConcurrentCollections {
    public static void main(String[] args) throws Exception {
        String[] words = "the cat and the hat and the bat".split(" ");

        // 1) ConcurrentHashMap.merge is atomic per key: 4 threads x 10,000 passes over the words
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        runInThreads(4, () -> { for (int i = 0; i < 10_000; i++) for (String w : words) counts.merge(w, 1, Integer::sum); });
        System.out.println("ConcurrentHashMap merge : " + new TreeMap<>(counts));

        // 2) Same with a synchronizedMap and get-then-put: each call is safe, the COMBINATION is not
        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        runInThreads(4, () -> { for (int i = 0; i < 10_000; i++) for (String w : words) {
            Integer old = syncMap.get(w);                           // check ...
            syncMap.put(w, old == null ? 1 : old + 1);              // ... then act
        }});
        System.out.println("synchronizedMap get+put : " + new TreeMap<>(syncMap) + "   [nondeterministic - lost updates; expected the=120000]");

        // 3) computeIfAbsent: create-once per key, atomically
        ConcurrentHashMap<String, List<String>> groups = new ConcurrentHashMap<>();
        runInThreads(4, () -> { for (String w : words) groups.computeIfAbsent(w.substring(0, 1), k -> new CopyOnWriteArrayList<>()).add(w); });
        System.out.println("computeIfAbsent sizes   : " + new TreeMap<>(Map.of("a", groups.get("a").size(), "b", groups.get("b").size(),
                "c", groups.get("c").size(), "h", groups.get("h").size(), "t", groups.get("t").size())));
        try {
            counts.put("x", null);
        } catch (NullPointerException e) {
            System.out.println("ConcurrentHashMap rejects null values -> NullPointerException");
        }

        // 4) Iteration while modifying: ArrayList fails fast, CopyOnWriteArrayList iterates a snapshot
        List<String> plain = new ArrayList<>(List.of("a", "b", "c"));
        try {
            for (String s : plain) if (s.equals("a")) plain.add("d");
        } catch (ConcurrentModificationException e) {
            System.out.println("ArrayList modify during for-each -> ConcurrentModificationException");
        }
        List<String> cow = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));
        int seen = 0;
        for (String s : cow) { seen++; if (s.equals("a")) cow.add("d"); }
        System.out.println("CopyOnWriteArrayList: iterator saw " + seen + " elements, list now " + cow);

        // 5) BlockingQueue: producer-consumer without writing wait/notify yourself
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 5; i++) queue.put("job-" + i); queue.put("STOP"); }    // put blocks when full
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        List<String> consumed = new ArrayList<>();
        Thread consumer = new Thread(() -> {
            try { for (String j; !(j = queue.take()).equals("STOP"); ) consumed.add(j); }       // take blocks when empty
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start(); producer.join(); consumer.join();
        System.out.println("BlockingQueue consumed  : " + consumed);
        System.out.println("offer on full queue     : " + fillAndOffer());
        System.out.println("poll with timeout, empty: " + new LinkedBlockingQueue<String>().poll(50, TimeUnit.MILLISECONDS));

        // 6) Sorted and lock-free structures
        ConcurrentSkipListMap<Integer, String> leaderboard = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        runInThreads(3, () -> { leaderboard.put(ThreadLocalRandom.current().nextInt(1, 1000), "p"); });
        System.out.println("ConcurrentSkipListMap sorted descending? " + isSortedDesc(leaderboard.keySet()));
        ConcurrentLinkedQueue<Integer> clq = new ConcurrentLinkedQueue<>();
        runInThreads(4, () -> { for (int i = 0; i < 1000; i++) clq.offer(i); });
        System.out.println("ConcurrentLinkedQueue size after 4 x 1000 offers = " + clq.size());
    }

    static String fillAndOffer() {
        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(1);
        q.offer(1);
        return "offer(2) = " + q.offer(2) + " (returns false instead of blocking)";
    }

    static boolean isSortedDesc(Set<Integer> keys) {
        Integer prev = null;
        for (Integer k : keys) { if (prev != null && prev < k) return false; prev = k; }
        return true;
    }

    static void runInThreads(int n, Runnable r) throws InterruptedException {
        Thread[] ts = new Thread[n];
        for (int i = 0; i < n; i++) (ts[i] = new Thread(r)).start();
        for (Thread t : ts) t.join();
    }
}
