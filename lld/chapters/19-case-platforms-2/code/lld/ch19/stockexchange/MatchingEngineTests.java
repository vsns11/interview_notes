package lld.ch19.stockexchange;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Tiny test harness (no JUnit - standard library only). Each test throws AssertionError on failure. */
final class MatchingEngineTests {
    private MatchingEngineTests() { }

    interface Test { void run() throws Exception; }

    record Case(String name, Test body) { }

    static int runAll() {
        List<Case> tests = List.of(
                new Case("sample input gives the expected four trades", MatchingEngineTests::sample),
                new Case("better price wins over earlier time", MatchingEngineTests::pricePriority),
                new Case("same price: earlier order fills first", MatchingEngineTests::timePriority),
                new Case("no trade when prices do not cross", MatchingEngineTests::noCross),
                new Case("partial fill leaves the remainder resting", MatchingEngineTests::partialFill),
                new Case("cancelled order is never matched", MatchingEngineTests::cancel),
                new Case("symbols never match each other", MatchingEngineTests::symbolIsolation),
                new Case("duplicate id and bad quantity rejected", MatchingEngineTests::validation),
                new Case("concurrent submits conserve quantity", MatchingEngineTests::concurrency));
        int failed = 0;
        for (Case t : tests) {
            try {
                t.body().run();
                System.out.println("  PASS  " + t.name());
            } catch (Exception | AssertionError e) {
                failed++;
                System.out.println("  FAIL  " + t.name() + " -> " + e.getMessage());
            }
        }
        System.out.println("  " + (tests.size() - failed) + "/" + tests.size() + " passed");
        return failed;
    }

    // ---------- helpers ----------
    private static MatchingEngine engine(List<Trade> sink) {
        return new MatchingEngine(ExecutionPricePolicy.SELL_PRICE, sink::add);
    }

    private static void eq(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
    }

    private static List<String> str(List<Trade> trades) { return trades.stream().map(Trade::toString).toList(); }

    private static final LocalTime T = LocalTime.of(10, 0);

    // ---------- tests ----------
    static void sample() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        Main.SAMPLE.forEach(e::submitLine);
        eq(List.of("#3 237.45 90 #2", "#3 236.00 20 #6", "#4 236.00 10 #6", "#5 236.00 20 #6"), str(out));
    }

    static void pricePriority() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("s1", T, "X", Side.SELL, new BigDecimal("101"), 10);   // earlier but worse
        e.submit("s2", T, "X", Side.SELL, new BigDecimal("100"), 10);   // later but cheaper
        e.submit("b1", T, "X", Side.BUY, new BigDecimal("105"), 10);
        eq(List.of("b1 100 10 s2"), str(out));
    }

    static void timePriority() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("b1", T, "X", Side.BUY, new BigDecimal("50"), 5);
        e.submit("b2", T, "X", Side.BUY, new BigDecimal("50"), 5);
        e.submit("s1", T, "X", Side.SELL, new BigDecimal("49"), 7);
        eq(List.of("b1 49 5 s1", "b2 49 2 s1"), str(out));
        eq(3, e.book("X").restingQty(Side.BUY));
    }

    static void noCross() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("b1", T, "X", Side.BUY, new BigDecimal("99.99"), 5);
        e.submit("s1", T, "X", Side.SELL, new BigDecimal("100.00"), 5);
        eq(0, out.size());
        eq(5, e.book("X").restingQty(Side.BUY));
        eq(5, e.book("X").restingQty(Side.SELL));
    }

    static void partialFill() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("s1", T, "X", Side.SELL, new BigDecimal("10"), 100);
        e.submit("b1", T, "X", Side.BUY, new BigDecimal("10"), 30);
        e.submit("b2", T, "X", Side.BUY, new BigDecimal("10"), 30);
        eq(40, e.book("X").restingQty(Side.SELL));
        eq(0, e.book("X").restingQty(Side.BUY));
    }

    static void cancel() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("s1", T, "X", Side.SELL, new BigDecimal("10"), 10);
        e.submit("s2", T, "X", Side.SELL, new BigDecimal("10"), 10);
        eq(true, e.cancel("s1"));
        eq(false, e.cancel("s1"));
        e.submit("b1", T, "X", Side.BUY, new BigDecimal("10"), 10);
        eq(List.of("b1 10 10 s2"), str(out));
    }

    static void symbolIsolation() {
        List<Trade> out = new ArrayList<>();
        MatchingEngine e = engine(out);
        e.submit("s1", T, "AAA", Side.SELL, new BigDecimal("1"), 10);
        e.submit("b1", T, "BBB", Side.BUY, new BigDecimal("5"), 10);
        eq(0, out.size());
    }

    static void validation() {
        MatchingEngine e = engine(new ArrayList<>());
        e.submit("x1", T, "X", Side.BUY, new BigDecimal("1"), 1);
        try { e.submit("x1", T, "X", Side.BUY, new BigDecimal("1"), 1); throw new AssertionError("dup accepted"); }
        catch (IllegalArgumentException expected) { /* ok */ }
        try { e.submit("x2", T, "X", Side.BUY, new BigDecimal("1"), 0); throw new AssertionError("qty 0 accepted"); }
        catch (IllegalArgumentException expected) { /* ok */ }
    }

    /** 8 threads x 500 orders on one symbol. Every share bought must equal every share sold. */
    static void concurrency() throws InterruptedException {
        List<Trade> out = Collections.synchronizedList(new ArrayList<>());
        MatchingEngine e = engine(out);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger buys = new AtomicInteger();
        AtomicInteger sells = new AtomicInteger();
        for (int t = 0; t < 8; t++) {
            int thread = t;
            pool.submit(() -> {
                go.await();
                for (int i = 0; i < 500; i++) {
                    Side side = (i + thread) % 2 == 0 ? Side.BUY : Side.SELL;
                    int qty = 1 + (i % 7);
                    (side == Side.BUY ? buys : sells).addAndGet(qty);
                    e.submit("t" + thread + "-" + i, T, "X", side, new BigDecimal(100 + (i % 3)), qty);
                }
                return null;
            });
        }
        go.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(30, TimeUnit.SECONDS)) throw new AssertionError("timeout");
        int traded = out.stream().mapToInt(Trade::quantity).sum();
        eq(buys.get(), traded + e.book("X").restingQty(Side.BUY));
        eq(sells.get(), traded + e.book("X").restingQty(Side.SELL));
        // the book must never be left crossed: best bid < best ask
        OrderBook book = e.book("X");
        if (book.bestBid().isPresent() && book.bestAsk().isPresent()
                && book.bestBid().get().compareTo(book.bestAsk().get()) >= 0)
            throw new AssertionError("book left crossed: " + book.snapshot());
    }
}
