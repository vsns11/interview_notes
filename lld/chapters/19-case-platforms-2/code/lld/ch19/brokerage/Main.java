package lld.ch19.brokerage;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        StockInventory inv = new StockInventory();
        inv.list("AAPL", Money.of("10.00"));
        inv.list("MSFT", Money.of("300.00"));
        inv.list("AMZN", Money.of("120.00"));
        // 70 shares offered per price level, levels 5 cents apart, 3 levels deep
        StockExchange ex = new StockExchange(inv, 70, new BigDecimal("0.05"), 3);
        Member alice = new Member("M1", "Alice", "alice@mail.test", ex, inv, new EmailNotifier());
        Admin admin = new Admin();

        System.out.println("== 1. Fund the account ==");
        System.out.println("  wire 5,000        -> " + alice.transfer(new WireTransfer(Money.of("5000"), Direction.DEPOSIT)));
        System.out.println("  check 200,000     -> " + alice.transfer(new CheckTransfer(Money.of("200000"), Direction.DEPOSIT))
                + " (over the 100,000 limit)");
        System.out.println("  cash = " + Money.fmt(alice.cash()));

        System.out.println("\n== 2. Watchlists and search ==");
        alice.watchlist("tech").add("AAPL");
        alice.watchlist("tech").add("MSFT");
        alice.watchlist("retail").add("AMZN");
        alice.watchlists().forEach(w -> System.out.println("  " + w.render(inv)));
        System.out.println("  search 'A' -> " + inv.search("A"));

        System.out.println("\n== 3. Market buy 100 AAPL fills in two parts (two lots) ==");
        Order m1 = new MarketOrder("O1", "M1", "AAPL", Side.BUY, 100);
        System.out.println("  place -> " + alice.place(m1));
        System.out.println("  " + m1 + " parts=" + m1.parts());

        System.out.println("\n== 4. Next day: limit buy 50 AAPL @ 9.50 waits, then fills when price drops ==");
        ex.setDay(2);
        Order l1 = new LimitOrder("O2", "M1", "AAPL", Side.BUY, 50, Money.of("9.50"), TimeInForce.GOOD_TILL_CANCELLED);
        System.out.println("  place -> " + alice.place(l1) + ", " + l1 + ", reserved " + Money.fmt(alice.reserved()));
        ex.tick("AAPL", Money.of("9.40"));
        System.out.println("  after tick 9.40: " + l1 + ", reserved " + Money.fmt(alice.reserved()));
        System.out.println("  holdings " + alice.holdings());

        System.out.println("\n== 5. Day 3: price rises, sell 120 AAPL (FIFO across lots) ==");
        ex.setDay(3);
        ex.tick("AAPL", Money.of("11.00"));
        Order s1 = new LimitOrder("O3", "M1", "AAPL", Side.SELL, 120, Money.of("10.90"), TimeInForce.GOOD_TILL_CANCELLED);
        System.out.println("  place -> " + alice.place(s1) + ", " + s1);
        System.out.println("  holdings " + alice.holdings());

        System.out.println("\n== 6. Protect the rest with a stop-loss @ 10.00, then the price falls ==");
        ex.setDay(4);
        Order stop = new StopLossOrder("O4", "M1", "AAPL", Side.SELL, 30, Money.of("10.00"));
        System.out.println("  place -> " + alice.place(stop) + ", " + stop);
        ex.tick("AAPL", Money.of("10.50"));
        System.out.println("  tick 10.50: " + stop);
        ex.tick("AAPL", Money.of("9.95"));
        System.out.println("  tick 9.95: " + stop + " parts=" + stop.parts());

        System.out.println("\n== 7. Stop-limit buy triggers but its limit is not met ==");
        Order sl = new StopLimitOrder("O5", "M1", "MSFT", Side.BUY, 5, Money.of("305.00"), Money.of("306.00"));
        System.out.println("  place -> " + alice.place(sl) + ", " + sl);
        ex.tick("MSFT", Money.of("310.00"));
        System.out.println("  tick 310 (stop hit, above limit): " + sl);
        ex.tick("MSFT", Money.of("305.50"));
        System.out.println("  tick 305.50 (within limit): " + sl);

        System.out.println("\n== 8. Reserved funds block over-spending, cancel releases them ==");
        System.out.println("  cash " + Money.fmt(alice.cash()) + ", available " + Money.fmt(alice.availableFunds()));
        Order big1 = new LimitOrder("O6", "M1", "AMZN", Side.BUY, 20, Money.of("100.00"), TimeInForce.GOOD_TILL_CANCELLED);
        Order big2 = new LimitOrder("O7", "M1", "AMZN", Side.BUY, 20, Money.of("100.00"), TimeInForce.GOOD_TILL_CANCELLED);
        System.out.println("  O6 buy 20 AMZN @ 100 -> " + alice.place(big1) + ", available " + Money.fmt(alice.availableFunds()));
        System.out.println("  O7 buy 20 AMZN @ 100 -> " + alice.place(big2));
        System.out.println("  cancel O6 -> " + alice.cancel(big1) + ", available " + Money.fmt(alice.availableFunds()));
        System.out.println("  sell 10 AMZN (none owned) -> "
                + alice.place(new MarketOrder("O8", "M1", "AMZN", Side.SELL, 10)));

        System.out.println("\n== 9. Blocked account cannot trade ==");
        admin.block(alice);
        System.out.println("  -> " + alice.place(new MarketOrder("O9", "M1", "AAPL", Side.BUY, 1)));
        admin.unblock(alice);

        System.out.println("\n== 10. Reports ==");
        System.out.print(alice.statement(new QuarterlyStatement(1, 90)));
        System.out.print(alice.statement(new TaxStatement(1, 365)));
    }
}
