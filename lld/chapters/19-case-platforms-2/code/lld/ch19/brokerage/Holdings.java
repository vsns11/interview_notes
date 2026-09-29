package lld.ch19.brokerage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** One purchase at one price. Kept apart from other purchases because tax depends on it. */
final class StockLot {
    private final String lotId;
    private final BigDecimal buyPrice;
    private final int day;
    private int quantity;

    StockLot(String lotId, int quantity, BigDecimal buyPrice, int day) {
        this.lotId = lotId; this.quantity = quantity; this.buyPrice = buyPrice; this.day = day;
    }
    String lotId() { return lotId; }
    int quantity() { return quantity; }
    BigDecimal buyPrice() { return buyPrice; }
    int day() { return day; }
    void take(int q) { quantity -= q; }
    @Override public String toString() { return lotId + ":" + quantity + "@" + buyPrice; }
}

/** What came out of one lot in one sale - exactly what a tax statement needs. */
record LotSale(String lotId, int quantity, BigDecimal buyPrice, BigDecimal sellPrice) {
    BigDecimal gain() { return Money.times(sellPrice.subtract(buyPrice), quantity); }
}

/** All lots of one symbol. Sells from the named lot first, then oldest first (FIFO). */
final class StockPosition {
    private final String symbol;
    private final List<StockLot> lots = new ArrayList<>();

    StockPosition(String symbol) { this.symbol = symbol; }

    String symbol() { return symbol; }
    void addLot(StockLot lot) { lots.add(lot); }
    int quantity() { return lots.stream().mapToInt(StockLot::quantity).sum(); }
    List<StockLot> lots() { return List.copyOf(lots); }

    List<LotSale> sell(int qty, BigDecimal price, Optional<String> preferredLot) {
        if (qty > quantity()) throw new IllegalStateException("not enough shares");
        List<LotSale> out = new ArrayList<>();
        List<StockLot> order = new ArrayList<>(lots);
        preferredLot.flatMap(id -> lots.stream().filter(l -> l.lotId().equals(id)).findFirst())
                    .ifPresent(l -> { order.remove(l); order.add(0, l); });
        int left = qty;
        for (StockLot lot : order) {
            if (left == 0) break;
            int take = Math.min(left, lot.quantity());
            if (take == 0) continue;
            lot.take(take);
            out.add(new LotSale(lot.lotId(), take, lot.buyPrice(), price));
            left -= take;
        }
        for (Iterator<StockLot> it = lots.iterator(); it.hasNext(); ) if (it.next().quantity() == 0) it.remove();
        return out;
    }
}

/** A watchlist holds symbols, not Stock objects, so it never goes stale. */
final class Watchlist {
    private final String name;
    private final Set<String> symbols = new LinkedHashSet<>();
    Watchlist(String name) { this.name = name; }
    String name() { return name; }
    void add(String symbol) { symbols.add(symbol); }
    void remove(String symbol) { symbols.remove(symbol); }
    Set<String> symbols() { return Set.copyOf(symbols); }
    String render(StockInventory inv) {
        StringBuilder sb = new StringBuilder(name + ":");
        symbols.forEach(s -> sb.append(' ').append(s).append('=').append(inv.priceOf(s)));
        return sb.toString();
    }
}

/** One executed fill as the member sees it. Sells carry the lots they consumed. */
record Trade(int day, String symbol, Side side, int quantity, BigDecimal price, List<LotSale> sales) { }

/** Template Method: the base class filters trades by day range, subclasses write the lines. */
abstract class Statement {
    private final int fromDay;
    private final int toDay;
    Statement(int fromDay, int toDay) { this.fromDay = fromDay; this.toDay = toDay; }

    final String render(String memberName, List<Trade> all) {
        List<Trade> inRange = all.stream().filter(t -> t.day() >= fromDay && t.day() <= toDay).toList();
        StringBuilder sb = new StringBuilder(title() + " for " + memberName + " (days " + fromDay + "-" + toDay + ")\n");
        body(inRange, sb);
        return sb.toString();
    }
    abstract String title();
    abstract void body(List<Trade> trades, StringBuilder sb);
}

final class QuarterlyStatement extends Statement {
    QuarterlyStatement(int from, int to) { super(from, to); }
    @Override String title() { return "Quarterly statement"; }
    @Override void body(List<Trade> trades, StringBuilder sb) {
        for (Trade t : trades)
            sb.append(String.format("  day %d %-4s %-4s %4d @ %s%n", t.day(), t.side(), t.symbol(), t.quantity(), t.price()));
    }
}

final class TaxStatement extends Statement {
    TaxStatement(int from, int to) { super(from, to); }
    @Override String title() { return "Tax statement"; }
    @Override void body(List<Trade> trades, StringBuilder sb) {
        BigDecimal total = Money.of("0");
        for (Trade t : trades) for (LotSale s : t.sales()) {
            sb.append(String.format("  %s %s: %d sh bought @ %s sold @ %s gain %s%n",
                    t.symbol(), s.lotId(), s.quantity(), s.buyPrice(), s.sellPrice(), Money.fmt(s.gain())));
            total = total.add(s.gain());
        }
        sb.append("  realised gain: ").append(Money.fmt(total)).append('\n');
    }
}
