package lld.ch15.concert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Numbered seats, lock-free. Each seat is an AtomicReference<SeatState>; a claim is one CAS.
 * Many buyers on DIFFERENT seats never wait for each other; on the SAME seat exactly one CAS wins.
 */
final class SeatedInventory {
    private final Map<String, Tier> tierOf = new LinkedHashMap<>();
    private final Map<String, AtomicReference<SeatState>> seats = new ConcurrentHashMap<>();

    void addSeat(String id, Tier tier) {
        tierOf.put(id, tier);
        seats.put(id, new AtomicReference<>(new Free()));
    }
    Tier tierOf(String id) { return tierOf.get(id); }

    /** All-or-nothing over several seats: claim in sorted order, roll back our own claims on failure. */
    boolean tryHold(List<String> ids, String holdId, String userId, Instant now, Instant expiresAt) {
        List<String> taken = new ArrayList<>();
        for (String id : ids.stream().sorted().toList()) {
            if (!tryHoldOne(id, holdId, userId, now, expiresAt)) {
                taken.forEach(t -> release(t, holdId));
                return false;
            }
            taken.add(id);
        }
        return true;
    }

    private boolean tryHoldOne(String id, String holdId, String userId, Instant now, Instant expiresAt) {
        AtomicReference<SeatState> ref = seats.get(id);
        if (ref == null) throw new IllegalArgumentException("no seat " + id);
        while (true) {
            SeatState cur = ref.get();
            boolean claimable = cur instanceof Free || (cur instanceof Held h && h.expired(now));
            if (!claimable) return false;
            if (ref.compareAndSet(cur, new Held(holdId, userId, expiresAt))) return true;
            // CAS lost: someone changed the seat between get() and CAS. Re-read and decide again.
        }
    }

    /** Held-by-this-hold and not expired -> Sold. Fails if the hold was lost. */
    boolean confirm(String id, String holdId, Instant now) {
        AtomicReference<SeatState> ref = seats.get(id);
        SeatState cur = ref.get();
        return cur instanceof Held h && h.holdId().equals(holdId) && !h.expired(now)
                && ref.compareAndSet(cur, new Sold(holdId, h.userId()));
    }

    /** Give the seat back only if it is still ours (held or sold by this hold). */
    boolean release(String id, String holdId) {
        AtomicReference<SeatState> ref = seats.get(id);
        SeatState cur = ref.get();
        boolean ours = (cur instanceof Held h && h.holdId().equals(holdId))
                || (cur instanceof Sold s && s.holdId().equals(holdId));
        return ours && ref.compareAndSet(cur, new Free());
    }

    /** Seats of a tier a fan could claim right now (Free, or Held but expired), in seat order. */
    List<String> freeSeats(Tier tier, Instant now) {
        List<String> out = new ArrayList<>();
        tierOf.forEach((id, t) -> {
            SeatState s = seats.get(id).get();
            if (t == tier && (s instanceof Free || (s instanceof Held h && h.expired(now)))) out.add(id);
        });
        return out;
    }

    long soldCount() { return seats.values().stream().filter(r -> r.get() instanceof Sold).count(); }

    SeatState stateOf(String id) { return seats.get(id).get(); }
}

/** General admission: no seat numbers, just a counter. A CAS loop means it can never go below zero. */
final class GaPool {
    private final AtomicInteger available;
    GaPool(int capacity) { available = new AtomicInteger(capacity); }

    boolean tryTake(int qty) {
        while (true) {
            int cur = available.get();
            if (cur < qty) return false;
            if (available.compareAndSet(cur, cur - qty)) return true;
        }
    }
    void giveBack(int qty) { available.addAndGet(qty); }
    int available()        { return available.get(); }
}

/** Anti-scalping: at most `max` tickets per user per concert, checked-and-updated atomically. */
final class PurchaseLimiter {
    private final int max;
    private final ConcurrentHashMap<String, Integer> used = new ConcurrentHashMap<>();
    PurchaseLimiter(int max) { this.max = max; }

    boolean tryUse(String userId, int qty) {
        boolean[] ok = {false};
        used.compute(userId, (u, cur) -> {
            int c = cur == null ? 0 : cur;
            if (c + qty > max) return cur;             // unchanged
            ok[0] = true;
            return c + qty;
        });
        return ok[0];
    }
    void giveBack(String userId, int qty) { used.computeIfPresent(userId, (u, c) -> c - qty <= 0 ? null : c - qty); }
}

/**
 * Virtual waiting room. Everyone who arrives gets a strictly increasing number; the system admits
 * people in that order, a batch at a time. First come, first served, and bots gain nothing by
 * hammering the buy button, because only admitted users may hold inventory.
 */
final class VirtualWaitingRoom {
    private final AtomicLong nextNumber = new AtomicLong();
    private final ConcurrentLinkedQueue<String> line = new ConcurrentLinkedQueue<>();
    private final Set<String> admitted = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> numberOf = new ConcurrentHashMap<>();

    /** synchronized so the number order and the line order can never disagree. */
    synchronized long join(String userId) {
        Long n = numberOf.get(userId);
        if (n != null) return n;                        // joining twice keeps your place
        long mine = nextNumber.incrementAndGet();
        numberOf.put(userId, mine);
        line.add(userId);
        return mine;
    }
    synchronized List<String> admitNext(int k) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            String u = line.poll();
            if (u == null) break;
            admitted.add(u);
            out.add(u);
        }
        return out;
    }
    boolean isAdmitted(String userId) { return admitted.contains(userId); }
    void admitDirectly(String userId) { admitted.add(userId); }
}
