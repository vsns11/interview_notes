package lld.ch19.auction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/** Observer: who wants to hear about bids and results. */
interface AuctionListener {
    default void onNewHighBid(AuctionListing a, Bid bid) { }
    default void onOutbid(AuctionListing a, User outbid, BigDecimal newPrice) { }
    default void onWon(AuctionListing a, Bid winning) { }
    default void onUnsold(AuctionListing a, boolean hadBids) { }
}

final class ConsoleNotifier implements AuctionListener {
    private volatile boolean muted;
    void mute(boolean m) { muted = m; }
    private void say(String to, String msg) { if (!muted) System.out.println("    [notify " + to + "] " + msg); }

    @Override public void onOutbid(AuctionListing a, User u, BigDecimal p) {
        say(u.name(), "you were outbid on " + a.itemName() + ", now " + p);
    }
    @Override public void onWon(AuctionListing a, Bid w) {
        say(w.bidder().name(), "you WON " + a.itemName() + " for " + w.amount());
        say(a.seller().name(), a.itemName() + " sold to " + w.bidder().name());
    }
    @Override public void onUnsold(AuctionListing a, boolean hadBids) {
        say(a.seller().name(), a.itemName() + " unsold" + (hadBids ? " (reserve not met)" : " (no bids)"));
    }
}

/** Search criteria, built up fluently. Each criterion is a Predicate (Specification pattern). */
final class AuctionQuery {
    private final List<Predicate<AuctionListing>> parts = new ArrayList<>();
    AuctionQuery nameContains(String s) {
        parts.add(a -> a.itemName().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))); return this;
    }
    AuctionQuery category(String c) { parts.add(a -> a.category().equalsIgnoreCase(c)); return this; }
    AuctionQuery priceBetween(BigDecimal lo, BigDecimal hi) {
        parts.add(a -> a.currentPrice().compareTo(lo) >= 0 && a.currentPrice().compareTo(hi) <= 0); return this;
    }
    Predicate<AuctionListing> toPredicate() { return parts.stream().reduce(x -> true, Predicate::and); }
}

/** Facade over listings, bidding and closing. Listeners are called OUTSIDE listing locks. */
final class AuctionService {
    private final InstantSource clock;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, AuctionListing> listings = new ConcurrentHashMap<>();
    private final List<AuctionListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger listingSeq = new AtomicInteger();
    private final AtomicInteger userSeq = new AtomicInteger();
    private final AtomicLong bidSeq = new AtomicLong();

    AuctionService(InstantSource clock) { this.clock = clock; }

    void subscribe(AuctionListener l) { listeners.add(l); }

    User register(String name, String email) {
        User u = new User("U" + userSeq.incrementAndGet(), name, email);
        if (users.putIfAbsent(u.id(), u) != null) throw new IllegalStateException("id clash");
        return u;
    }

    AuctionListing createListing(User seller, String item, String description, String category,
                                 BigDecimal startingPrice, BigDecimal increment, BigDecimal reserve, Duration duration) {
        AuctionListing a = new AuctionListing("A" + listingSeq.incrementAndGet(), item, description, category, seller,
                startingPrice, increment, reserve, clock.instant(), duration);
        listings.put(a.id(), a);
        return a;
    }

    List<AuctionListing> search(AuctionQuery q) {
        return listings.values().stream()
                .filter(a -> a.status(clock.instant()) == AuctionStatus.ACTIVE)
                .filter(q.toPredicate())
                .sorted(Comparator.comparing(AuctionListing::endsAt))
                .toList();
    }

    BidResult placeBid(String listingId, User bidder, BigDecimal amount) {
        AuctionListing a = listings.get(listingId);
        if (a == null) throw new IllegalArgumentException("no listing " + listingId);
        BidResult r = a.placeBid(bidder, amount, clock.instant(), bidSeq.incrementAndGet());
        r.accepted().ifPresent(top -> {                 // snapshot taken inside the lock
            listeners.forEach(l -> l.onNewHighBid(a, top));
            r.outbid().ifPresent(u -> listeners.forEach(l -> l.onOutbid(a, u, top.amount())));
        });
        return r;
    }

    /** Run periodically (e.g. by a ScheduledExecutorService). Closes every auction whose time is up. */
    List<CloseResult> closeExpired() {
        List<CloseResult> closed = new ArrayList<>();
        for (AuctionListing a : listings.values()) {
            Optional<CloseResult> r = a.closeIfDue(clock.instant());
            r.ifPresent(closed::add);
        }
        for (CloseResult r : closed) {
            if (r.winningBid().isPresent()) listeners.forEach(l -> l.onWon(r.listing(), r.winningBid().get()));
            else listeners.forEach(l -> l.onUnsold(r.listing(), r.listing().highestBid().isPresent()));
        }
        return closed;
    }
}
