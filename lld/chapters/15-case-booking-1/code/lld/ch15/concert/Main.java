package lld.ch15.concert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Instant.parse("2026-06-01T09:00:00Z"));
        AtomicReference<BigDecimal> collected = new AtomicReference<>(BigDecimal.ZERO);
        PaymentGateway bank = new PaymentGateway() {
            public boolean charge(String u, BigDecimal a) { collected.accumulateAndGet(a, BigDecimal::add); return true; }
            public void refund(String u, BigDecimal a)    { collected.accumulateAndGet(a, BigDecimal::subtract); }
        };
        Notifier notifier = (u, m) -> System.out.println("  [notify " + u + "] " + m);

        Concert show = new Concert("C1", "The Band", "Stadium", LocalDateTime.of(2026, 8, 15, 19, 30));
        SeatedInventory seated = new SeatedInventory();
        for (int i = 1; i <= 4; i++) seated.addSeat("VIP-" + i, Tier.VIP);
        for (int i = 1; i <= 6; i++) seated.addSeat("P-" + i, Tier.PREMIUM);
        GaPool ga = new GaPool(100);
        VirtualWaitingRoom room = new VirtualWaitingRoom();
        TicketingService svc = new TicketingService(show, seated, ga, new PurchaseLimiter(4), room,
                bank, notifier, clock, Duration.ofMinutes(10));

        System.out.println("== 1. Seating and availability, then the virtual waiting room ==");
        System.out.println("  availability: " + svc.availability());
        for (String u : List.of("ana", "ben", "cai", "dev", "eli")) System.out.println("  " + u + " got number " + room.join(u));
        System.out.println("  ben joins again, keeps number " + room.join("ben"));
        System.out.println("  admitted batch 1: " + room.admitNext(3));
        try { svc.holdGa("eli", 1); } catch (IllegalStateException e) { System.out.println("  REFUSED: " + e.getMessage()); }

        System.out.println("\n== 2. 40 admitted fans CAS for the same seat VIP-1 ==");
        AtomicInteger vipWins = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 40; i++) {
                String u = "vip-fan-" + i;
                room.admitDirectly(u);
                pool.submit(() -> {
                    go.await();
                    try { svc.holdSeats(u, List.of("VIP-1")); vipWins.incrementAndGet(); }
                    catch (SoldOutException ignored) { }
                    return null;
                });
            }
            go.countDown();
        }
        System.out.println("  holds on VIP-1 = " + vipWins.get());

        System.out.println("\n== 3. Flash sale: 1,000 fans, 100 GA places (98 left after test buys) ==");
        Hold anaHold = svc.holdGa("ana", 2);
        svc.purchase(anaHold.id());
        AtomicInteger sold = new AtomicInteger(), soldOut = new AtomicInteger();
        CountDownLatch go2 = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 1000; i++) {
                String u = "fan-" + i;
                room.admitDirectly(u);
                pool.submit(() -> {
                    go2.await();
                    try { svc.purchase(svc.holdGa(u, 1).id()); sold.incrementAndGet(); }
                    catch (SoldOutException e) { soldOut.incrementAndGet(); }
                    return null;
                });
            }
            go2.countDown();
        }
        System.out.println("  sold=" + sold + " soldOut=" + soldOut + " gaLeft=" + svc.gaAvailable() + " (never negative)");

        System.out.println("\n== 4. Per-user limit (4) stops a scalper ==");
        Hold p = svc.holdSeats("ben", List.of("P-1", "P-2", "P-3"));
        System.out.println("  ben holds " + p.seatIds() + " = " + p.amount());
        try { svc.holdSeats("ben", List.of("P-4", "P-5")); } catch (IllegalStateException e) { System.out.println("  REFUSED: " + e.getMessage()); }
        List<Ticket> benTickets = svc.purchase(p.id());
        System.out.println("  ben bought " + benTickets.stream().map(Ticket::seatOrGa).toList());

        System.out.println("\n== 5. Sold out -> waitlist -> a cancellation is offered to the waitlist ==");
        try { svc.holdGa("cai", 1); } catch (SoldOutException e) { System.out.println("  cai: " + e.getMessage() + ", joins waitlist"); }
        svc.joinWaitlist("cai", Tier.GENERAL_ADMISSION);
        svc.cancelOrder(anaHold.id());                  // ana returns her 2 GA tickets
        System.out.println("  GA left after offers = " + svc.gaAvailable());

        System.out.println("\n== 6. Unpaid holds expire; the reaper returns them ==");
        System.out.println("  admitted batch 2: " + room.admitNext(3));
        Hold forgotten = svc.holdSeats("dev", List.of("P-6"));
        clock.advance(Duration.ofMinutes(11));
        System.out.println("  expired holds reaped = " + svc.expireHolds() + " (VIP-1, cai's unpaid offer, dev's P-6)");
        try { svc.purchase(forgotten.id()); } catch (IllegalStateException e) { System.out.println("  dev: " + e.getMessage()); }
        System.out.println("  P-6 now " + seated.stateOf("P-6") + ", GA left " + svc.gaAvailable());
        System.out.println("  availability: " + svc.availability());
        System.out.println("  money collected = " + collected.get());
    }
}
