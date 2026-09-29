package lld.ch12.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/** ConcurrentHashMap atomic compound ops, CopyOnWriteArrayList snapshots, BlockingQueue. */
public class Main {

    public static void main(String[] args) throws Exception {
        // ---------- ConcurrentHashMap ----------
        ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            Thread th = new Thread(() -> {
                for (int rep = 0; rep < 1_000; rep++)
                    for (String w : words) wordCount.merge(w, 1, Integer::sum);   // atomic per key
            });
            ts.add(th); th.start();
        }
        for (Thread th : ts) th.join();
        System.out.println("word counts (4 threads x 1000 reps): " + new TreeMap<>(wordCount));

        // computeIfAbsent: create-once per key, even under races (e.g. one lock/queue per user).
        ConcurrentHashMap<String, List<String>> ordersByUser = new ConcurrentHashMap<>();
        ordersByUser.computeIfAbsent("alice", k -> new CopyOnWriteArrayList<>()).add("order-1");
        ordersByUser.computeIfAbsent("alice", k -> new CopyOnWriteArrayList<>()).add("order-2");
        System.out.println("alice's orders: " + ordersByUser.get("alice"));

        // WRONG pattern (check-then-act across two calls) vs RIGHT (one atomic call):
        //   if (!map.containsKey(k)) map.put(k, v);   // two threads can both put
        //   map.putIfAbsent(k, v);                    // atomic
        Map<String, String> seats = new ConcurrentHashMap<>();
        System.out.println("first putIfAbsent returns : " + seats.putIfAbsent("A1", "bob"));
        System.out.println("second putIfAbsent returns: " + seats.putIfAbsent("A1", "eve") + "  (seat already bob's)");

        // ---------- CopyOnWriteArrayList ----------
        CopyOnWriteArrayList<String> listeners = new CopyOnWriteArrayList<>(List.of("L1", "L2"));
        int seen = 0;
        for (String l : listeners) {          // iterates over a snapshot
            listeners.add(l + "-new");        // no ConcurrentModificationException
            seen++;
        }
        System.out.println("iterated " + seen + " items; list now " + listeners);

        // ---------- BlockingQueue ----------
        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(2);   // bounded = back-pressure
        System.out.println("offer 1: " + q.offer(1) + ", offer 2: " + q.offer(2) + ", offer 3: " + q.offer(3) + " (full)");
        System.out.println("take -> " + q.take() + ", poll -> " + q.poll() + ", poll(50ms) on empty -> "
                + q.poll(50, TimeUnit.MILLISECONDS));
    }
}
