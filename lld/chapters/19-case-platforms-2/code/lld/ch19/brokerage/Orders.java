package lld.ch19.brokerage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

enum Side { BUY, SELL }

enum OrderStatus { OPEN, PARTIALLY_FILLED, FILLED, CANCELLED }

/** Time in force: how long an order may stay alive. Modelled, only GTC used in the demo. */
enum TimeInForce { GOOD_TILL_CANCELLED, FILL_OR_KILL, IMMEDIATE_OR_CANCEL, ON_THE_OPEN, ON_THE_CLOSE }

final class Money {
    private Money() { }
    static BigDecimal of(String s) { return new BigDecimal(s).setScale(2, RoundingMode.HALF_EVEN); }
    static BigDecimal times(BigDecimal price, int qty) {
        return price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_EVEN);
    }
    static String fmt(BigDecimal b) { return "$" + b.setScale(2, RoundingMode.HALF_EVEN).toPlainString(); }
}

/** One fill. A 100-share order might be filled as 70 @ 10.00 plus 30 @ 10.05. */
record OrderPart(int quantity, BigDecimal price, int day) { }

/**
 * Everything the four order types share. They differ in exactly two answers:
 * (1) may I trade at this market price? (2) what is the worst price I accept?
 */
abstract class Order {
    private final String id;
    private final String memberId;
    private final String symbol;
    private final Side side;
    private final int quantity;
    private final TimeInForce timeInForce;
    private final List<OrderPart> parts = new ArrayList<>();
    private OrderStatus status = OrderStatus.OPEN;

    Order(String id, String memberId, String symbol, Side side, int quantity, TimeInForce tif) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        this.id = id;
        this.memberId = memberId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.timeInForce = tif;
    }

    /** Question 1. Market and limit orders always say yes; stop orders wait for the trigger. */
    abstract boolean canTradeAt(BigDecimal marketPrice);

    /** Question 2. Empty means "any price" (market behaviour). */
    abstract Optional<BigDecimal> limitPrice();

    abstract String type();

    /** Is a fill at this price acceptable? BUY: price <= limit. SELL: price >= limit. */
    final boolean acceptsPrice(BigDecimal price) {
        return limitPrice().map(l -> side == Side.BUY ? price.compareTo(l) <= 0 : price.compareTo(l) >= 0)
                           .orElse(true);
    }

    final synchronized void addPart(OrderPart p) {
        if (p.quantity() > remaining()) throw new IllegalStateException("overfill");
        parts.add(p);
        status = remaining() == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    final synchronized boolean cancel() {
        if (status == OrderStatus.FILLED || status == OrderStatus.CANCELLED) return false;
        status = OrderStatus.CANCELLED;
        return true;
    }

    final synchronized int filled() { return parts.stream().mapToInt(OrderPart::quantity).sum(); }
    final synchronized int remaining() { return quantity - filled(); }
    final synchronized OrderStatus status() { return status; }
    final synchronized boolean isOpen() { return status == OrderStatus.OPEN || status == OrderStatus.PARTIALLY_FILLED; }
    final synchronized List<OrderPart> parts() { return List.copyOf(parts); }

    final String id() { return id; }
    final String memberId() { return memberId; }
    final String symbol() { return symbol; }
    final Side side() { return side; }
    final int quantity() { return quantity; }
    final TimeInForce timeInForce() { return timeInForce; }

    @Override public String toString() {
        return id + " " + type() + " " + side + " " + quantity + " " + symbol + " [" + status() + "]";
    }
}

final class MarketOrder extends Order {
    MarketOrder(String id, String memberId, String symbol, Side side, int qty) {
        super(id, memberId, symbol, side, qty, TimeInForce.GOOD_TILL_CANCELLED);
    }
    @Override boolean canTradeAt(BigDecimal p) { return true; }
    @Override Optional<BigDecimal> limitPrice() { return Optional.empty(); }
    @Override String type() { return "MARKET"; }
}

final class LimitOrder extends Order {
    private final BigDecimal limit;
    LimitOrder(String id, String memberId, String symbol, Side side, int qty, BigDecimal limit, TimeInForce tif) {
        super(id, memberId, symbol, side, qty, tif);
        this.limit = limit;
    }
    @Override boolean canTradeAt(BigDecimal p) { return true; }
    @Override Optional<BigDecimal> limitPrice() { return Optional.of(limit); }
    @Override String type() { return "LIMIT@" + limit; }
}

/**
 * Stop (stop-loss) order: sleeps until the market reaches the stop price, then trades
 * like a market order. SELL stop fires when price <= stop, BUY stop when price >= stop.
 * Once triggered it stays triggered.
 */
class StopLossOrder extends Order {
    private final BigDecimal stopPrice;
    private boolean triggered;

    StopLossOrder(String id, String memberId, String symbol, Side side, int qty, BigDecimal stopPrice) {
        super(id, memberId, symbol, side, qty, TimeInForce.GOOD_TILL_CANCELLED);
        this.stopPrice = stopPrice;
    }

    @Override final synchronized boolean canTradeAt(BigDecimal p) {
        if (!triggered) {
            triggered = side() == Side.SELL ? p.compareTo(stopPrice) <= 0 : p.compareTo(stopPrice) >= 0;
        }
        return triggered;
    }
    @Override Optional<BigDecimal> limitPrice() { return Optional.empty(); }
    @Override String type() { return "STOP@" + stopPrice; }
    BigDecimal stopPrice() { return stopPrice; }
}

/** A stop order that becomes a LIMIT order (not a market order) once triggered. */
final class StopLimitOrder extends StopLossOrder {
    private final BigDecimal limit;
    StopLimitOrder(String id, String memberId, String symbol, Side side, int qty, BigDecimal stop, BigDecimal limit) {
        super(id, memberId, symbol, side, qty, stop);
        this.limit = limit;
    }
    @Override Optional<BigDecimal> limitPrice() { return Optional.of(limit); }
    @Override String type() { return "STOP@" + stopPrice() + "/LIMIT@" + limit; }
}
