package lld.ch19.stockexchange;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Routes each order to its symbol's book. One lock per book: orders for different
 * symbols match in parallel, orders for the same symbol are strictly serialised
 * (which is what makes "time priority" well defined).
 */
final class MatchingEngine {
    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final Map<String, String> symbolOfOrder = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final ExecutionPricePolicy policy;
    private final Consumer<Trade> tradeSink;

    MatchingEngine(ExecutionPricePolicy policy, Consumer<Trade> tradeSink) {
        this.policy = policy;
        this.tradeSink = tradeSink;
    }

    List<Trade> submit(String id, LocalTime time, String symbol, Side side, BigDecimal price, int qty) {
        if (qty <= 0 || price.signum() <= 0) throw new IllegalArgumentException("bad quantity/price: " + id);
        if (symbolOfOrder.putIfAbsent(id, symbol) != null) throw new IllegalArgumentException("duplicate order id " + id);
        OrderBook book = books.computeIfAbsent(symbol, s -> new OrderBook(s, policy));
        List<Trade> trades;
        synchronized (book) {
            // the sequence is taken inside the lock, so arrival order == matching order
            Order o = new Order(id, time, symbol, side, price, qty, sequence.incrementAndGet());
            trades = book.add(o);
        }
        trades.forEach(tradeSink);        // publish outside the lock
        return trades;
    }

    boolean cancel(String orderId) {
        String symbol = symbolOfOrder.get(orderId);
        if (symbol == null) return false;
        OrderBook book = books.get(symbol);
        synchronized (book) { return book.cancel(orderId); }
    }

    OrderBook book(String symbol) { return books.get(symbol); }

    /** Parses "#1 09:45 BAC sell 240.12 100" and submits it. */
    List<Trade> submitLine(String line) {
        String[] f = line.trim().split("\\s+");
        if (f.length != 6) throw new IllegalArgumentException("bad line: " + line);
        return submit(f[0], LocalTime.parse(f[1]), f[2], Side.valueOf(f[3].toUpperCase()),
                new BigDecimal(f[4]), Integer.parseInt(f[5]));
    }
}
