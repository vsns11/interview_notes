package lld.ch19.stockexchange;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

enum Side { BUY, SELL }

/** An order. Only remainingQty changes, and only under the book's lock. */
final class Order {
    private final String id;
    private final LocalTime time;
    private final String symbol;
    private final Side side;
    private final BigDecimal price;
    private final long sequence;           // arrival order: breaks ties when times are equal
    private int remainingQty;

    Order(String id, LocalTime time, String symbol, Side side, BigDecimal price, int qty, long sequence) {
        if (qty <= 0) throw new IllegalArgumentException("quantity must be positive: " + id);
        if (price.signum() <= 0) throw new IllegalArgumentException("price must be positive: " + id);
        this.id = id; this.time = time; this.symbol = symbol; this.side = side;
        this.price = price; this.remainingQty = qty; this.sequence = sequence;
    }

    String id() { return id; }
    LocalTime time() { return time; }
    String symbol() { return symbol; }
    Side side() { return side; }
    BigDecimal price() { return price; }
    long sequence() { return sequence; }
    int remainingQty() { return remainingQty; }
    void fill(int qty) { remainingQty -= qty; }
    boolean isDone() { return remainingQty == 0; }
}

/** Output line: "<buy_order_id> <execution_price> <quantity> <sell_order_id>". */
record Trade(String buyOrderId, BigDecimal price, int quantity, String sellOrderId) {
    @Override public String toString() { return buyOrderId + " " + price.toPlainString() + " " + quantity + " " + sellOrderId; }
}

/** Who sets the trade price. The Navi problem uses the SELL order's price; real exchanges use the resting order's. */
interface ExecutionPricePolicy {
    BigDecimal price(Order buy, Order sell, Order incoming);

    ExecutionPricePolicy SELL_PRICE = (buy, sell, incoming) -> sell.price();
    ExecutionPricePolicy RESTING_PRICE = (buy, sell, incoming) -> incoming == buy ? sell.price() : buy.price();
}

/**
 * The order book for ONE symbol. Each side is a sorted map of price level -> FIFO queue.
 * Bids: highest price first. Asks: lowest price first. Inside a level: first come, first served.
 * That is price-time priority. Not thread-safe by itself: the engine locks it.
 */
final class OrderBook {
    private final String symbol;
    private final NavigableMap<BigDecimal, Deque<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    private final NavigableMap<BigDecimal, Deque<Order>> asks = new TreeMap<>();
    private final Map<String, Order> resting = new HashMap<>();
    private final ExecutionPricePolicy pricePolicy;

    OrderBook(String symbol, ExecutionPricePolicy pricePolicy) {
        this.symbol = symbol;
        this.pricePolicy = pricePolicy;
    }

    /** Match the incoming order against the other side, then rest whatever is left. */
    List<Trade> add(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        NavigableMap<BigDecimal, Deque<Order>> opposite = incoming.side() == Side.BUY ? asks : bids;

        while (!incoming.isDone() && !opposite.isEmpty()) {
            Map.Entry<BigDecimal, Deque<Order>> best = opposite.firstEntry();
            if (!crosses(incoming, best.getKey())) break;          // best price does not meet ours: stop
            Deque<Order> queue = best.getValue();
            Iterator<Order> it = queue.iterator();
            while (!incoming.isDone() && it.hasNext()) {
                Order restingOrder = it.next();                      // oldest first = time priority
                int qty = Math.min(incoming.remainingQty(), restingOrder.remainingQty());
                Order buy = incoming.side() == Side.BUY ? incoming : restingOrder;
                Order sell = incoming.side() == Side.SELL ? incoming : restingOrder;
                trades.add(new Trade(buy.id(), pricePolicy.price(buy, sell, incoming), qty, sell.id()));
                incoming.fill(qty);
                restingOrder.fill(qty);
                if (restingOrder.isDone()) {
                    it.remove();
                    resting.remove(restingOrder.id());
                }
            }
            if (queue.isEmpty()) opposite.pollFirstEntry();
        }
        if (!incoming.isDone()) rest(incoming);
        return trades;
    }

    boolean cancel(String orderId) {
        Order o = resting.remove(orderId);
        if (o == null) return false;
        NavigableMap<BigDecimal, Deque<Order>> side = o.side() == Side.BUY ? bids : asks;
        Deque<Order> q = side.get(o.price());
        q.remove(o);
        if (q.isEmpty()) side.remove(o.price());
        return true;
    }

    int restingQty(Side side) {
        return (side == Side.BUY ? bids : asks).values().stream()
                .flatMap(Deque::stream).mapToInt(Order::remainingQty).sum();
    }

    Optional<BigDecimal> bestBid() { return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstKey()); }

    Optional<BigDecimal> bestAsk() { return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstKey()); }

    String snapshot() {
        return symbol + " bids=" + levels(bids) + " asks=" + levels(asks);
    }

    private static boolean crosses(Order incoming, BigDecimal bestOpposite) {
        return incoming.side() == Side.BUY
                ? incoming.price().compareTo(bestOpposite) >= 0     // buy meets or beats the ask
                : incoming.price().compareTo(bestOpposite) <= 0;    // sell meets or undercuts the bid
    }

    private void rest(Order o) {
        (o.side() == Side.BUY ? bids : asks).computeIfAbsent(o.price(), p -> new ArrayDeque<>()).addLast(o);
        resting.put(o.id(), o);
    }

    private static String levels(NavigableMap<BigDecimal, Deque<Order>> side) {
        List<String> out = new ArrayList<>();
        side.forEach((p, q) -> out.add(p.toPlainString() + "x" + q.stream().mapToInt(Order::remainingQty).sum()));
        return out.toString();
    }
}
