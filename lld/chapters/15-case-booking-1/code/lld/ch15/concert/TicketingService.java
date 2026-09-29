package lld.ch15.concert;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flow: join waiting room -> get admitted -> HOLD (seats or GA qty, with TTL) -> PURCHASE.
 * Released inventory (expired hold, cancellation) is offered to the tier's waitlist first.
 */
final class TicketingService {
    private final Concert concert;
    private final SeatedInventory seated;
    private final GaPool ga;
    private final PurchaseLimiter limiter;
    private final VirtualWaitingRoom room;
    private final PaymentGateway payments;
    private final Notifier notifier;
    private final Clock clock;
    private final Duration holdTtl;
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();           // active holds
    private final Map<String, List<Ticket>> orders = new ConcurrentHashMap<>();  // holdId -> tickets
    private final Map<Tier, Queue<String>> waitlists = new EnumMap<>(Tier.class);
    private final AtomicInteger seq = new AtomicInteger();

    TicketingService(Concert concert, SeatedInventory seated, GaPool ga, PurchaseLimiter limiter,
                     VirtualWaitingRoom room, PaymentGateway payments, Notifier notifier, Clock clock, Duration holdTtl) {
        this.concert = concert; this.seated = seated; this.ga = ga; this.limiter = limiter; this.room = room;
        this.payments = payments; this.notifier = notifier; this.clock = clock; this.holdTtl = holdTtl;
        for (Tier t : Tier.values()) waitlists.put(t, new ConcurrentLinkedQueue<>());
    }

    // ------------------------------------------------------------------ hold
    Hold holdSeats(String userId, List<String> seatIds) {
        requireAdmitted(userId);
        if (seatIds.isEmpty()) throw new IllegalArgumentException("no seats");
        Tier tier = seated.tierOf(seatIds.get(0));
        if (seatIds.stream().anyMatch(id -> seated.tierOf(id) != tier))
            throw new IllegalArgumentException("one hold = one tier");
        if (!limiter.tryUse(userId, seatIds.size()))
            throw new IllegalStateException(userId + " would exceed the per-user ticket limit");
        Instant now = clock.instant();
        Hold h = new Hold("H" + seq.incrementAndGet(), userId, tier, List.copyOf(seatIds), seatIds.size(),
                tier.priceFor(seatIds.size()), now.plus(holdTtl));
        if (!seated.tryHold(seatIds, h.id(), userId, now, h.expiresAt())) {
            limiter.giveBack(userId, seatIds.size());
            throw new SoldOutException(seatIds + " not available");
        }
        holds.put(h.id(), h);
        return h;
    }

    Hold holdGa(String userId, int qty) {
        requireAdmitted(userId);
        if (!limiter.tryUse(userId, qty))
            throw new IllegalStateException(userId + " would exceed the per-user ticket limit");
        if (!ga.tryTake(qty)) {
            limiter.giveBack(userId, qty);
            throw new SoldOutException("general admission sold out");
        }
        Hold h = new Hold("H" + seq.incrementAndGet(), userId, Tier.GENERAL_ADMISSION, List.of(), qty,
                Tier.GENERAL_ADMISSION.priceFor(qty), clock.instant().plus(holdTtl));
        holds.put(h.id(), h);
        return h;
    }

    // ------------------------------------------------------------------ purchase
    List<Ticket> purchase(String holdId) {
        Hold h = holds.remove(holdId);                    // atomic claim: a hold is purchased at most once
        if (h == null) throw new IllegalStateException(holdId + " unknown, expired or already used");
        if (h.expired(clock.instant())) { releaseInventory(h); throw new IllegalStateException(holdId + " expired"); }
        if (!payments.charge(h.userId(), h.amount())) { releaseInventory(h); throw new IllegalStateException("payment declined"); }

        List<Ticket> tickets = new ArrayList<>();
        if (h.tier().seated) {
            Instant now = clock.instant();
            List<String> confirmed = new ArrayList<>();
            for (String id : h.seatIds()) {
                if (!seated.confirm(id, h.id(), now)) {   // hold lost during payment -> compensate
                    confirmed.forEach(c -> seated.release(c, h.id()));
                    h.seatIds().forEach(s -> seated.release(s, h.id()));
                    limiter.giveBack(h.userId(), h.quantity());
                    payments.refund(h.userId(), h.amount());
                    throw new IllegalStateException("seat " + id + " lost during payment, refunded");
                }
                confirmed.add(id);
                tickets.add(new Ticket("T" + seq.incrementAndGet(), concert.id(), h.userId(), h.tier(), id));
            }
        } else {
            for (int i = 0; i < h.quantity(); i++)
                tickets.add(new Ticket("T" + seq.incrementAndGet(), concert.id(), h.userId(), h.tier(), "GA"));
        }
        orders.put(h.id(), List.copyOf(tickets));
        return tickets;
    }

    // ------------------------------------------------------------------ release paths
    /** The reaper: run every few seconds in production. Returns how many holds it expired. */
    int expireHolds() {
        Instant now = clock.instant();
        int n = 0;
        for (Hold h : holds.values())
            if (h.expired(now) && holds.remove(h.id(), h)) { releaseInventory(h); n++; }
        return n;
    }

    /** Cancel a whole order (all tickets bought with one hold). */
    void cancelOrder(String holdId) {
        List<Ticket> tickets = orders.remove(holdId);
        if (tickets == null) throw new IllegalStateException("no order " + holdId);
        Tier tier = tickets.get(0).tier();
        BigDecimal refund = tier.priceFor(tickets.size());
        payments.refund(tickets.get(0).userId(), refund);
        Hold asIfHeld = new Hold(holdId, tickets.get(0).userId(), tier,
                tier.seated ? tickets.stream().map(Ticket::seatOrGa).toList() : List.of(), tickets.size(), refund, clock.instant());
        releaseInventory(asIfHeld);
    }

    void joinWaitlist(String userId, Tier tier) { waitlists.get(tier).add(userId); }

    private void releaseInventory(Hold h) {
        limiter.giveBack(h.userId(), h.quantity());
        if (h.tier().seated) {
            h.seatIds().forEach(id -> seated.release(id, h.id()));
            h.seatIds().forEach(id -> offerSeat(h.tier(), id));
        } else {
            ga.giveBack(h.quantity());
            offerGa();
        }
    }

    /** Waitlist first: freed inventory goes to the person who has waited longest, as a fresh hold. */
    private void offerGa() {
        Queue<String> q = waitlists.get(Tier.GENERAL_ADMISSION);
        String next;
        while (ga.available() > 0 && (next = q.poll()) != null) {
            room.admitDirectly(next);
            try {
                Hold h = holdGa(next, 1);
                notifier.send(next, "a GA ticket freed up, held for you as " + h.id() + " until " + h.expiresAt());
            } catch (RuntimeException e) { /* over their limit or lost the race: try the next person */ }
        }
    }

    private void offerSeat(Tier tier, String seatId) {
        String next = waitlists.get(tier).poll();
        if (next == null) return;
        room.admitDirectly(next);
        try {
            Hold h = holdSeats(next, List.of(seatId));
            notifier.send(next, "seat " + seatId + " freed up, held for you as " + h.id());
        } catch (RuntimeException e) { /* someone else took it first */ }
    }

    private void requireAdmitted(String userId) {
        if (!room.isAdmitted(userId)) throw new IllegalStateException(userId + " is still in the waiting room");
    }

    int gaAvailable() { return ga.available(); }

    /** What a fan sees before choosing: free places per tier (requirement: view seating and availability). */
    String availability() {
        StringBuilder sb = new StringBuilder();
        for (Tier t : Tier.values()) {
            if (t.seated) sb.append(t).append(" free ").append(seated.freeSeats(t, clock.instant())).append(", ");
            else sb.append(t).append(" free ").append(ga.available());
        }
        return sb.toString();
    }
}
