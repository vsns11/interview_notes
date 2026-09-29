package lld.ch19.grocery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    /** Fake card/UPI network: declines one blocked card, approves everything else. */
    static final class FakeGateway implements PaymentGateway {
        private final AtomicInteger seq = new AtomicInteger(1000);
        @Override public String authorize(Tender t) {
            if (t instanceof Card c && c.last4().equals("0005")) throw new PosException("Card **0005 declined by issuer");
            return "AUTH" + seq.incrementAndGet();
        }
        @Override public void refund(String auth, BigDecimal amount) {
            System.out.println("   gateway: reversed " + Money.fmt(amount) + " on " + auth);
        }
    }

    static final String MILK = Ean13.withCheckDigit("890100000001"), PANEER = Ean13.withCheckDigit("890100000002"),
            CHIPS = Ean13.withCheckDigit("890100000003"), BISCUITS = Ean13.withCheckDigit("890100000004"),
            DETERGENT = Ean13.withCheckDigit("890100000005"), BANANAS = "4011", TOMATOES = "4664";

    public static void main(String[] args) throws Exception {
        Catalog catalog = new Catalog();
        catalog.add(new Product("BAN", BANANAS, "Bananas", Department.PRODUCE, PricingUnit.PER_KG, Money.of("60"), TaxCategory.EXEMPT));
        catalog.add(new Product("TOM", TOMATOES, "Tomatoes", Department.PRODUCE, PricingUnit.PER_KG, Money.of("40"), TaxCategory.EXEMPT));
        catalog.add(new Product("MLK", MILK, "Milk 1L", Department.DAIRY, PricingUnit.EACH, Money.of("68"), TaxCategory.EXEMPT));
        catalog.add(new Product("PNR", PANEER, "Paneer 200g", Department.DAIRY, PricingUnit.EACH, Money.of("90"), TaxCategory.REDUCED));
        catalog.add(new Product("CHP", CHIPS, "Chips 50g", Department.SNACKS, PricingUnit.EACH, Money.of("20"), TaxCategory.STANDARD));
        catalog.add(new Product("BIS", BISCUITS, "Biscuits", Department.SNACKS, PricingUnit.EACH, Money.of("30"), TaxCategory.REDUCED));
        catalog.add(new Product("DET", DETERGENT, "Detergent 1kg", Department.HOUSEHOLD, PricingUnit.EACH, Money.of("245"), TaxCategory.STANDARD));

        List<ItemPromotion> offers = List.of(
                new BuyXGetYFree("CHP", 2, 1),                       // saves 20 on 3 packs
                new MultiBuy("CHP", 3, Money.of("50")),               // saves 10 on 3 packs: loses to the one above
                new MultiBuy("BIS", 3, Money.of("80")),
                new PercentOffDepartment(Department.PRODUCE, 10));
        List<BasketPromotion> coupons = List.of(new SpendThresholdCoupon("SAVE50", Money.of("500"), Money.of("50")));
        PricingEngine engine = new PricingEngine(offers, coupons, new TaxInclusive());

        Inventory stock = new Inventory();
        for (String sku : List.of("BAN", "TOM", "MLK", "CHP", "BIS", "DET")) stock.set(sku, "200", "20");
        stock.set("PNR", "100", "20");
        List<String> alerts = Collections.synchronizedList(new ArrayList<>());
        stock.addListener((sku, left, why) -> alerts.add(sku + " " + why + " (on hand " + left.stripTrailingZeros().toPlainString() + ")"));
        ReceiptStore receipts = new ReceiptStore();
        FakeGateway gateway = new FakeGateway();
        Register lane1 = new Register("LANE-1", catalog, engine, stock, receipts, gateway);

        System.out.println("== 1. Scan a basket ==");
        lane1.startSale();
        lane1.weigh(BANANAS, 1245);
        lane1.weigh(TOMATOES, 500);
        lane1.scan(MILK);
        lane1.scan(MILK);
        lane1.scan(PANEER);
        lane1.scan(CHIPS, 3);
        lane1.scan(BISCUITS, 4);
        lane1.scan(DETERGENT);
        lane1.voidLine(4);                                          // customer puts one milk back
        for (String bad : List.of(Ean13.withCheckDigit("890100000009"), "8901000000018", BANANAS)) {
            try { lane1.scan(bad); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        }
        lane1.coupon("SAVE50");

        System.out.println("\n== 2. Total, then split payment: declined card, UPI, cash with change ==");
        PriceBreakdown bill = lane1.total();
        System.out.println("   amount due " + Money.fmt(bill.total()));
        try { lane1.scan(MILK); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        try { lane1.pay(new Card("0005", Money.of("300"))); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        try { lane1.pay(new Upi("asha@okbank", Money.of("5000"))); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        System.out.println("   UPI 400 -> receipt? " + lane1.pay(new Upi("asha@okbank", Money.of("400"))));
        Receipt r1 = lane1.pay(new Cash(Money.of("500")));
        System.out.print(r1.render());

        System.out.println("\n== 3. Returns refund what was really paid ==");
        System.out.println("   return 1 of 3 chips -> refund " + Money.fmt(lane1.refund(r1.id(), "CHP", 1)));
        System.out.println("   return 2 more chips -> refund " + Money.fmt(lane1.refund(r1.id(), "CHP", 2))
                + "  (all three add up to the " + Money.fmt(r1.breakdown().netBySku().get("CHP")) + " paid)");
        try { lane1.refund(r1.id(), "CHP", 1); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        try { lane1.refund(r1.id(), "PNR", 2); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }

        System.out.println("\n== 4. Price change during a sale; going back to scanning; cancelling ==");
        lane1.startSale();
        lane1.scan(MILK);
        catalog.reprice(MILK, Money.of("72"));
        lane1.scan(MILK);
        SkuTotal milk = lane1.total().items().get(0);
        System.out.println("   2 milk scanned around a reprice: gross " + Money.fmt(milk.gross()) + " = Rs 68 (old price kept) + Rs 72 (new)");
        lane1.backToScanning();
        lane1.scan(PANEER);
        System.out.println("   after adding paneer, due " + Money.fmt(lane1.total().total()));
        lane1.pay(new Card("4242", Money.of("100")));
        try { lane1.backToScanning(); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }
        lane1.cancelSale();
        try { lane1.pay(new Cash(Money.of("10"))); } catch (PosException e) { System.out.println("   error: " + e.getMessage()); }

        System.out.println("\n== 5. Same basket, US-style tax added at the till ==");
        Sale same = new Sale("X");
        r1.lines().forEach(l -> { if (!l.voided()) same.add(l.product(), l.quantity()); });
        same.addCoupon("SAVE50");
        PriceBreakdown us = same.beginTender(new PricingEngine(offers, coupons, new TaxExclusive()));
        System.out.println("   inclusive total " + Money.fmt(r1.breakdown().total()) + " vs exclusive total " + Money.fmt(us.total())
                + " (tax " + us.taxByCategory() + ")");

        System.out.println("\n== 6. Four registers sell 30 paneer each at once; stock was 100 ==");
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            for (int i = 1; i <= 4; i++) {
                Register lane = new Register("LANE-" + (i + 1), catalog, engine, stock, receipts, gateway);
                pool.submit(() -> {
                    go.await();
                    for (int k = 0; k < 30; k++) {
                        lane.startSale();
                        lane.scan(PANEER);
                        lane.total();
                        lane.pay(new Cash(Money.of("100")));
                    }
                    return null;
                });
            }
            go.countDown();
        }
        System.out.println("   paneer on hand: " + stock.onHand("PNR") + "  (sold 120 + 1 earlier; the till never refuses a real item)");
        alerts.stream().sorted().forEach(a -> System.out.println("   alert: " + a));
    }
}
