package lld.ch19.brokerage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** A symbol and its latest price. */
final class Stock {
    private final String symbol;
    private volatile BigDecimal price;
    Stock(String symbol, BigDecimal price) { this.symbol = symbol; this.price = price; }
    String symbol() { return symbol; }
    BigDecimal price() { return price; }
    void setPrice(BigDecimal p) { price = p; }
}

/** The one place prices are read from, so no two parts of the system disagree. */
final class StockInventory {
    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    void list(String symbol, BigDecimal price) { stocks.put(symbol, new Stock(symbol, price)); }
    Optional<Stock> find(String symbol) { return Optional.ofNullable(stocks.get(symbol)); }
    BigDecimal priceOf(String symbol) {
        return find(symbol).orElseThrow(() -> new IllegalArgumentException("unknown " + symbol)).price();
    }
    List<String> search(String prefix) {
        return stocks.keySet().stream().filter(s -> s.startsWith(prefix.toUpperCase())).sorted().toList();
    }
}

/** Observer: how the exchange calls back without knowing what a Member is. */
interface OrderListener {
    void onFill(Order order, OrderPart part);
    void onCancelled(Order order);
}

/**
 * A simulated exchange. At each price level it offers a fixed depth of shares
 * (e.g. 70 @ price, 70 @ price+0.05 ...) so that fills come back in parts, like a
 * real order book. (A real matching engine is built in lld.ch19.stockexchange.)
 * All methods are synchronized: one lock orders every event on the exchange.
 */
final class StockExchange {
    private final StockInventory inventory;
    private final int depthPerLevel;
    private final BigDecimal tickSize;
    private final int levels;
    private final Map<String, List<Order>> openOrders = new ConcurrentHashMap<>();
    private final Map<String, OrderListener> listeners = new ConcurrentHashMap<>();
    private int day = 1;

    StockExchange(StockInventory inventory, int depthPerLevel, BigDecimal tickSize, int levels) {
        this.inventory = inventory;
        this.depthPerLevel = depthPerLevel;
        this.tickSize = tickSize;
        this.levels = levels;
    }

    synchronized void register(String memberId, OrderListener l) { listeners.put(memberId, l); }
    synchronized void setDay(int d) { day = d; }
    synchronized int day() { return day; }

    synchronized void submit(Order o) {
        openOrders.computeIfAbsent(o.symbol(), k -> new ArrayList<>()).add(o);
        tryFill(o, inventory.priceOf(o.symbol()));
        prune(o.symbol());
    }

    /** A new price arrives: every open order on that symbol gets another chance (stops trigger here). */
    synchronized void tick(String symbol, BigDecimal newPrice) {
        inventory.find(symbol).orElseThrow().setPrice(newPrice);
        for (Order o : List.copyOf(openOrders.getOrDefault(symbol, List.of()))) tryFill(o, newPrice);
        prune(symbol);
    }

    synchronized boolean cancel(Order o) {
        if (!o.cancel()) return false;
        prune(o.symbol());
        listeners.get(o.memberId()).onCancelled(o);
        return true;
    }

    private void tryFill(Order o, BigDecimal market) {
        if (!o.isOpen() || !o.canTradeAt(market)) return;
        for (int k = 0; k < levels && o.remaining() > 0; k++) {
            BigDecimal step = tickSize.multiply(BigDecimal.valueOf(k));
            BigDecimal levelPrice = o.side() == Side.BUY ? market.add(step) : market.subtract(step);
            if (!o.acceptsPrice(levelPrice)) break;          // limit reached: rest stays open
            OrderPart part = new OrderPart(Math.min(depthPerLevel, o.remaining()), levelPrice, day);
            o.addPart(part);
            listeners.get(o.memberId()).onFill(o, part);
        }
    }

    private void prune(String symbol) {
        openOrders.getOrDefault(symbol, new ArrayList<>()).removeIf(o -> !o.isOpen());
    }
}
