package lld.ch21.wallet;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Clock clock = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC);
        WalletService svc = new WalletService(
                List.of(new FirstTransferOffer("10", "50.00"), new BigTransferOffer("1000.00", "25.00")), clock);

        svc.topUp(svc.promotions().id(), Money.of("100.00"), "budget-1");   // marketing funds the campaign
        Wallet alice = svc.createWallet("Alice");
        Wallet bob = svc.createWallet("Bob");

        System.out.println("== top-ups");
        svc.topUp(alice.id(), Money.of("2000.00"), "req-1");
        svc.topUp(bob.id(), Money.of("300.50"), "req-2");
        svc.topUp(alice.id(), Money.of("2000.00"), "req-1");       // client retry: same key, no double credit
        System.out.println("  Alice=" + alice.balance() + "  Bob=" + bob.balance());

        System.out.println("== transfers");
        TransferResult r1 = svc.transfer(alice.id(), bob.id(), Money.of("200.00"), "req-3");
        System.out.println("  " + r1);
        TransferResult r1again = svc.transfer(alice.id(), bob.id(), Money.of("200.00"), "req-3");
        System.out.println("  retry returns same result? " + (r1 == r1again));
        System.out.println("  " + svc.transfer(alice.id(), bob.id(), Money.of("1200.00"), "req-4"));
        System.out.println("  Alice=" + alice.balance() + "  Bob=" + bob.balance() + "  PROMO=" + svc.promotions().balance());

        System.out.println("== edge cases");
        attempt(() -> svc.transfer(bob.id(), alice.id(), Money.of("99999.00"), "req-5"));
        attempt(() -> svc.transfer(bob.id(), alice.id(), new BigDecimal("-5"), "req-6"));
        attempt(() -> svc.transfer(bob.id(), alice.id(), new BigDecimal("1.005"), "req-7"));
        attempt(() -> svc.transfer(bob.id(), bob.id(), Money.of("1.00"), "req-8"));
        attempt(() -> svc.transfer(bob.id(), "W99", Money.of("1.00"), "req-9"));
        System.out.println("  failed req-5 can be retried later: " + svc.transfer(bob.id(), alice.id(), Money.of("10.00"), "req-5").amount());

        System.out.println("== statement for Alice");
        alice.statement().forEach(t -> System.out.println("  " + t));
        System.out.println("== statement for PROMO");
        svc.promotions().statement().forEach(t -> System.out.println("  " + t));

        System.out.println("== concurrency: 1000 opposite transfers on 8 threads");
        BigDecimal before = svc.totalMoney();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 1000; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    if (n % 2 == 0) svc.transfer(alice.id(), bob.id(), Money.of("1.00"), "c-" + n);
                    else svc.transfer(bob.id(), alice.id(), Money.of("1.00"), "c-" + n);
                } catch (InsufficientFundsException ignored) { }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("  total before=" + before + " after=" + svc.totalMoney()
                + " conserved=" + (before.compareTo(svc.totalMoney()) == 0));
        System.out.println("  Alice=" + alice.balance() + "  Bob=" + bob.balance() + "  PROMO=" + svc.promotions().balance());
    }

    private static void attempt(Runnable r) {
        try { r.run(); System.out.println("  ok"); }
        catch (RuntimeException e) { System.out.println("  error: " + e.getMessage()); }
    }
}
