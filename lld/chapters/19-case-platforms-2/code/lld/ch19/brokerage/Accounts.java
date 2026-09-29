package lld.ch19.brokerage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

enum AccountStatus { ACTIVE, CLOSED, CANCELED, BLACKLISTED, NONE }

enum ReturnStatus { SUCCESS, FAIL, INSUFFICIENT_FUNDS, INSUFFICIENT_QUANTITY, NO_STOCK_POSITION, ACCOUNT_NOT_ACTIVE }

enum Direction { DEPOSIT, WITHDRAW }

/** Three ways to move money. They differ only in name and clearing time; the limit rule lives here once. */
abstract class MoneyTransfer {
    static final BigDecimal TRANSFER_LIMIT = Money.of("100000");
    private final BigDecimal amount;
    private final Direction direction;
    MoneyTransfer(BigDecimal amount, Direction direction) { this.amount = amount; this.direction = direction; }
    BigDecimal amount() { return amount; }
    Direction direction() { return direction; }
    boolean isValid() { return amount.signum() > 0 && amount.compareTo(TRANSFER_LIMIT) <= 0; }
    abstract String methodName();
    abstract int daysToClear();
}

final class CheckTransfer extends MoneyTransfer {
    CheckTransfer(BigDecimal a, Direction d) { super(a, d); }
    @Override String methodName() { return "check"; }
    @Override int daysToClear() { return 5; }
}

final class WireTransfer extends MoneyTransfer {
    WireTransfer(BigDecimal a, Direction d) { super(a, d); }
    @Override String methodName() { return "wire"; }
    @Override int daysToClear() { return 1; }
}

final class ElectronicBankTransfer extends MoneyTransfer {
    ElectronicBankTransfer(BigDecimal a, Direction d) { super(a, d); }
    @Override String methodName() { return "electronic bank transfer"; }
    @Override int daysToClear() { return 3; }
}

interface Notifier { void send(String to, String message); }

final class EmailNotifier implements Notifier {
    @Override public void send(String to, String message) { System.out.println("    [email " + to + "] " + message); }
}

/**
 * A member. Implements OrderListener so the exchange can call back without knowing
 * what a member is. Holds reserved funds so ten $1,000 orders cannot all pass a $1,000 check.
 * Lock order: the member never calls the exchange while holding its own lock.
 */
final class Member implements OrderListener {
    private final String id;
    private final String name;
    private final String email;
    private final StockExchange exchange;
    private final StockInventory inventory;
    private final Notifier notifier;
    private volatile AccountStatus status = AccountStatus.ACTIVE;

    private BigDecimal cash = Money.of("0");
    private final Map<String, BigDecimal> reservedFunds = new HashMap<>();   // buy orderId -> money still reserved
    private final Map<String, Order> pendingSells = new HashMap<>();            // sell orderId -> open sell order
    private final Map<String, StockPosition> positions = new LinkedHashMap<>();
    private final Map<String, Watchlist> watchlists = new LinkedHashMap<>();
    private final List<Trade> trades = new ArrayList<>();
    private int lotSeq = 1;

    Member(String id, String name, String email, StockExchange ex, StockInventory inv, Notifier n) {
        this.id = id; this.name = name; this.email = email;
        this.exchange = ex; this.inventory = inv; this.notifier = n;
        ex.register(id, this);
    }

    String id() { return id; }
    String name() { return name; }
    void setStatus(AccountStatus s) { status = s; }

    // ---- money ----
    synchronized ReturnStatus transfer(MoneyTransfer t) {
        if (!t.isValid()) return ReturnStatus.FAIL;
        if (t.direction() == Direction.WITHDRAW) {
            if (t.amount().compareTo(availableFunds()) > 0) return ReturnStatus.INSUFFICIENT_FUNDS;
            cash = cash.subtract(t.amount());
        } else {
            cash = cash.add(t.amount());
        }
        return ReturnStatus.SUCCESS;
    }

    synchronized BigDecimal cash() { return cash; }

    synchronized BigDecimal reserved() {
        BigDecimal r = Money.of("0");
        for (Map.Entry<String, BigDecimal> e : reservedFunds.entrySet()) r = r.add(e.getValue());
        return r;
    }

    synchronized BigDecimal availableFunds() { return cash.subtract(reserved()); }

    // ---- orders ----
    ReturnStatus place(Order o) {
        ReturnStatus check = reserveFor(o);                // under our lock
        if (check == ReturnStatus.SUCCESS) exchange.submit(o);   // outside our lock
        return check;
    }

    private synchronized ReturnStatus reserveFor(Order o) {
        if (status != AccountStatus.ACTIVE) return ReturnStatus.ACCOUNT_NOT_ACTIVE;
        if (o.side() == Side.BUY) {
            BigDecimal perShare = o.limitPrice().orElse(inventory.priceOf(o.symbol()));
            BigDecimal need = Money.times(perShare, o.quantity());
            if (need.compareTo(availableFunds()) > 0) return ReturnStatus.INSUFFICIENT_FUNDS;
            reservedFunds.put(o.id(), need);           // released part by part as fills arrive
            return ReturnStatus.SUCCESS;
        }
        StockPosition pos = positions.get(o.symbol());
        if (pos == null) return ReturnStatus.NO_STOCK_POSITION;
        int promised = pendingSells.values().stream()               // shares already promised to other
                .filter(p -> p.symbol().equals(o.symbol()))              // open sells of this symbol
                .mapToInt(Order::remaining).sum();
        if (o.quantity() > pos.quantity() - promised) return ReturnStatus.INSUFFICIENT_QUANTITY;
        pendingSells.put(o.id(), o);
        return ReturnStatus.SUCCESS;
    }

    ReturnStatus cancel(Order o) { return exchange.cancel(o) ? ReturnStatus.SUCCESS : ReturnStatus.FAIL; }

    /** Called by the exchange: this is where money and shares actually move. */
    @Override public synchronized void onFill(Order o, OrderPart part) {
        BigDecimal value = Money.times(part.price(), part.quantity());
        List<LotSale> sales = List.of();
        if (o.side() == Side.BUY) {
            cash = cash.subtract(value);
            BigDecimal left = reservedFunds.getOrDefault(o.id(), Money.of("0")).subtract(value);
            if (o.remaining() == 0 || left.signum() <= 0) reservedFunds.remove(o.id());
            else reservedFunds.put(o.id(), left);
            positions.computeIfAbsent(o.symbol(), StockPosition::new)
                     .addLot(new StockLot("L" + lotSeq++, part.quantity(), part.price(), part.day()));
        } else {
            cash = cash.add(value);
            if (o.remaining() == 0) pendingSells.remove(o.id());
            sales = positions.get(o.symbol()).sell(part.quantity(), part.price(), Optional.empty());
            if (positions.get(o.symbol()).quantity() == 0) positions.remove(o.symbol());
        }
        trades.add(new Trade(part.day(), o.symbol(), o.side(), part.quantity(), part.price(), sales));
        notifier.send(email, "filled " + part.quantity() + " " + o.symbol() + " @ " + part.price()
                + " (" + o.id() + " " + o.status() + ")");
    }

    @Override public synchronized void onCancelled(Order o) {
        reservedFunds.remove(o.id());
        pendingSells.remove(o.id());
        notifier.send(email, o.id() + " cancelled, " + o.remaining() + " shares unfilled");
    }

    // ---- watchlists, positions, reports ----
    synchronized Watchlist watchlist(String name) { return watchlists.computeIfAbsent(name, Watchlist::new); }
    synchronized List<Watchlist> watchlists() { return List.copyOf(watchlists.values()); }
    synchronized Map<String, List<StockLot>> holdings() {
        Map<String, List<StockLot>> m = new LinkedHashMap<>();
        positions.forEach((s, p) -> m.put(s, p.lots()));
        return m;
    }
    synchronized String statement(Statement s) { return s.render(name, List.copyOf(trades)); }
}

final class Admin {
    void block(Member m) { m.setStatus(AccountStatus.BLACKLISTED); }
    void unblock(Member m) { m.setStatus(AccountStatus.ACTIVE); }
}
