package lld.ch12.lldcases;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Thread-safe building blocks for three interview favourites. */
public class Main {

    // =====================================================================
    // 1. Parking lot: many gates allocate spots at the same time.
    // =====================================================================
    enum SpotType { COMPACT, LARGE }
    record Spot(String id, SpotType type) { }

    static class SpotAllocator {
        // One lock-free queue of FREE spots per type. poll() is atomic: two gates can never get the same spot.
        private final Map<SpotType, ConcurrentLinkedQueue<Spot>> free = new EnumMap<>(SpotType.class);
        private final ConcurrentHashMap<String, Spot> occupiedByTicket = new ConcurrentHashMap<>();

        SpotAllocator(List<Spot> spots) {
            for (SpotType t : SpotType.values()) free.put(t, new ConcurrentLinkedQueue<>());
            spots.forEach(s -> free.get(s.type()).add(s));   // EnumMap itself is never modified after this
        }
        Optional<Spot> park(String ticket, SpotType type) {
            Spot s = free.get(type).poll();
            if (s != null) occupiedByTicket.put(ticket, s);
            return Optional.ofNullable(s);
        }
        void leave(String ticket) {
            Spot s = occupiedByTicket.remove(ticket);        // remove is atomic: double-exit is harmless
            if (s != null) free.get(s.type()).offer(s);
        }
        // ConcurrentLinkedQueue.size() walks the queue (O(n)) and is only an estimate while other
        // threads change it; fine for a report once all gates are idle, not for allocation logic.
        int freeCount(SpotType t) { return free.get(t).size(); }
    }

    // =====================================================================
    // 2. Seat booking: all-or-nothing booking of several seats.
    // =====================================================================
    static class Show {
        private final Map<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
        private final Map<String, String> bookedBy = new ConcurrentHashMap<>();

        Show(Set<String> seatIds) { seatIds.forEach(id -> seatLocks.put(id, new ReentrantLock())); }

        /** Locks seats in sorted order (prevents deadlock), checks all, then books all. */
        boolean book(String user, Set<String> seats) {
            List<ReentrantLock> held = new ArrayList<>();
            try {
                for (String id : new TreeSet<>(seats)) {         // global order: A1 < A2 < A3
                    ReentrantLock l = seatLocks.get(id);
                    if (l == null) return false;                  // unknown seat
                    l.lock();
                    held.add(l);
                }
                for (String id : seats) if (bookedBy.containsKey(id)) return false;   // someone got one first
                for (String id : seats) bookedBy.put(id, user);
                return true;
            } finally {
                for (int i = held.size() - 1; i >= 0; i--) held.get(i).unlock();
            }
        }
        String owner(String seat) { return bookedBy.get(seat); }
    }

    // =====================================================================
    // 3. Inventory: never sell more than we have (no overselling).
    // =====================================================================
    static class Inventory {
        private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();

        void addStock(String sku, int qty) {
            stock.computeIfAbsent(sku, k -> new AtomicInteger()).addAndGet(qty);
        }
        /** CAS loop: decrement only if enough stock remains. */
        boolean reserve(String sku, int qty) {
            AtomicInteger s = stock.get(sku);
            if (s == null) return false;
            while (true) {
                int cur = s.get();
                if (cur < qty) return false;
                if (s.compareAndSet(cur, cur - qty)) return true;
            }
        }
        int available(String sku) { AtomicInteger s = stock.get(sku); return s == null ? 0 : s.get(); }
    }

    // ---------------------------------------------------------------------
    /** Run 'n' tasks that all start at the same instant (maximises contention). */
    static void raceAll(int n, java.util.function.IntConsumer task) throws InterruptedException {
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService ex = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < n; i++) {
                final int id = i;
                ex.submit(() -> { go.await(); task.accept(id); return null; });
            }
            go.countDown();
        } // try-with-resources close() waits for all tasks (Java 19+)
    }

    public static void main(String[] args) throws Exception {
        List<Spot> spots = new ArrayList<>();
        for (int i = 1; i <= 10; i++) spots.add(new Spot("C" + i, SpotType.COMPACT));
        SpotAllocator lot = new SpotAllocator(spots);
        AtomicInteger parked = new AtomicInteger();
        Set<String> handedOut = ConcurrentHashMap.newKeySet();
        raceAll(100, car -> lot.park("T" + car, SpotType.COMPACT).ifPresent(s -> { parked.incrementAndGet(); handedOut.add(s.id()); }));
        System.out.println("parking: 100 cars raced for 10 spots -> parked " + parked.get()
                + ", distinct spots " + handedOut.size() + ", free left " + lot.freeCount(SpotType.COMPACT));

        Show show = new Show(Set.of("A1", "A2", "A3", "A4"));
        AtomicInteger wins = new AtomicInteger();
        raceAll(50, u -> { if (show.book("user" + u, Set.of("A2"))) wins.incrementAndGet(); });
        System.out.println("booking: 50 users wanted seat A2 -> successful bookings " + wins.get());
        AtomicInteger groupWins = new AtomicInteger();
        raceAll(2, u -> { if (show.book(u == 0 ? "alice" : "bob", u == 0 ? Set.of("A3", "A4") : Set.of("A4", "A3"))) groupWins.incrementAndGet(); });
        System.out.println("booking: alice {A3,A4} vs bob {A4,A3} (opposite order) -> winners " + groupWins.get()
                + ", A3 and A4 same owner: " + show.owner("A3").equals(show.owner("A4")));

        Inventory inv = new Inventory();
        inv.addStock("iphone", 50);
        AtomicInteger sold = new AtomicInteger();
        raceAll(200, o -> { if (inv.reserve("iphone", 1)) sold.incrementAndGet(); });
        System.out.println("inventory: 200 buyers, 50 in stock -> sold " + sold.get() + ", left " + inv.available("iphone"));
    }
}
