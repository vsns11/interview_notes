package lld.ch19.auction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record User(String id, String name, String email) { }

/** One bid. seq breaks ties between bids with the same timestamp. */
record Bid(User bidder, BigDecimal amount, Instant at, long seq) { }

enum AuctionStatus { SCHEDULED, ACTIVE, CLOSED, CANCELLED }

/** Why a bid was refused - returned, not thrown, because refusals are normal business. */
enum BidOutcome { ACCEPTED, AUCTION_NOT_ACTIVE, AUCTION_ENDED, SELLER_CANNOT_BID, TOO_LOW, ALREADY_HIGHEST }

/** Result of placeBid: what happened, the bid that was accepted, who (if anyone) was just outbid. */
record BidResult(BidOutcome outcome, BigDecimal minimumNextBid, Optional<Bid> accepted, Optional<User> outbid) { }

/** Result of closing: sold to a winner, or unsold. */
record CloseResult(AuctionListing listing, Optional<Bid> winningBid, boolean reserveMet) { }

/**
 * One auction. All state changes go through synchronized methods, so the
 * "read highest bid, compare, replace" sequence is atomic: two bidders can never
 * both believe they are the highest.
 */
final class AuctionListing {
    private final String id;
    private final String itemName;
    private final String description;
    private final String category;
    private final User seller;
    private final BigDecimal startingPrice;
    private final BigDecimal minIncrement;
    private final BigDecimal reservePrice;          // hidden minimum; ZERO = no reserve
    private final Instant startsAt;
    private final Instant endsAt;
    private AuctionStatus status = AuctionStatus.SCHEDULED;
    private final List<Bid> bids = new ArrayList<>();
    private Bid highest;

    AuctionListing(String id, String itemName, String description, String category, User seller,
                   BigDecimal startingPrice, BigDecimal minIncrement, BigDecimal reservePrice,
                   Instant startsAt, Duration duration) {
        if (startingPrice.signum() <= 0 || minIncrement.signum() <= 0) throw new IllegalArgumentException("prices must be > 0");
        this.id = id; this.itemName = itemName; this.description = description; this.category = category;
        this.seller = seller; this.startingPrice = money(startingPrice); this.minIncrement = money(minIncrement);
        this.reservePrice = money(reservePrice); this.startsAt = startsAt; this.endsAt = startsAt.plus(duration);
    }

    /** Lazily moves SCHEDULED -> ACTIVE when the start time has passed. */
    private void refresh(Instant now) {
        if (status == AuctionStatus.SCHEDULED && !now.isBefore(startsAt)) status = AuctionStatus.ACTIVE;
    }

    synchronized BidResult placeBid(User bidder, BigDecimal rawAmount, Instant now, long seq) {
        refresh(now);
        BigDecimal amount = money(rawAmount);
        if (status != AuctionStatus.ACTIVE) return refuse(BidOutcome.AUCTION_NOT_ACTIVE);
        if (!now.isBefore(endsAt)) return refuse(BidOutcome.AUCTION_ENDED);  // closing job may not have run yet
        if (bidder.id().equals(seller.id())) return refuse(BidOutcome.SELLER_CANNOT_BID);
        if (highest != null && highest.bidder().id().equals(bidder.id())) return refuse(BidOutcome.ALREADY_HIGHEST);
        if (amount.compareTo(minimumNextBid()) < 0) return refuse(BidOutcome.TOO_LOW);

        Optional<User> outbid = Optional.ofNullable(highest).map(Bid::bidder);
        highest = new Bid(bidder, amount, now, seq);
        bids.add(highest);
        return new BidResult(BidOutcome.ACCEPTED, minimumNextBid(), Optional.of(highest), outbid);
    }

    /** First bid must reach the starting price; later bids must beat the leader by the increment. */
    synchronized BigDecimal minimumNextBid() {
        return highest == null ? startingPrice : highest.amount().add(minIncrement);
    }

    /** ACTIVE -> CLOSED once time is up. Returns empty if not yet due (or already closed). */
    synchronized Optional<CloseResult> closeIfDue(Instant now) {
        refresh(now);
        if (status != AuctionStatus.ACTIVE || now.isBefore(endsAt)) return Optional.empty();
        status = AuctionStatus.CLOSED;
        boolean reserveMet = highest != null && highest.amount().compareTo(reservePrice) >= 0;
        return Optional.of(new CloseResult(this, reserveMet ? Optional.of(highest) : Optional.empty(), reserveMet));
    }

    /** A seller may withdraw only while nobody has bid. */
    synchronized boolean cancel() {
        if (status == AuctionStatus.CLOSED || status == AuctionStatus.CANCELLED || highest != null) return false;
        status = AuctionStatus.CANCELLED;
        return true;
    }

    private BidResult refuse(BidOutcome why) { return new BidResult(why, minimumNextBid(), Optional.empty(), Optional.empty()); }

    private static BigDecimal money(BigDecimal b) { return b.setScale(2, RoundingMode.HALF_EVEN); }

    String id() { return id; }
    String itemName() { return itemName; }
    String description() { return description; }
    String category() { return category; }
    User seller() { return seller; }
    BigDecimal startingPrice() { return startingPrice; }
    synchronized AuctionStatus status(Instant now) { refresh(now); return status; }
    Instant endsAt() { return endsAt; }
    synchronized Optional<Bid> highestBid() { return Optional.ofNullable(highest); }
    synchronized BigDecimal currentPrice() { return highest == null ? startingPrice : highest.amount(); }
    synchronized List<Bid> bidHistory() { return List.copyOf(bids); }

    @Override public String toString() { return id + " '" + itemName + "' now " + currentPrice(); }
}

/** A controllable clock for demos and tests (production code passes InstantSource.system()). */
final class ManualClock implements InstantSource {
    private Instant now;
    ManualClock(Instant start) { now = start; }
    @Override public synchronized Instant instant() { return now; }
    synchronized void advance(Duration d) { now = now.plus(d); }
}
