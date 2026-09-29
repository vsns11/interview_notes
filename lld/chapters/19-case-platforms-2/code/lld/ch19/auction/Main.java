package lld.ch19.auction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        ManualClock clock = new ManualClock(Instant.parse("2026-09-28T10:00:00Z"));
        AuctionService svc = new AuctionService(clock);
        ConsoleNotifier notifier = new ConsoleNotifier();
        svc.subscribe(notifier);

        User sam = svc.register("Sam", "sam@mail.test");
        User ana = svc.register("Ana", "ana@mail.test");
        User bob = svc.register("Bob", "bob@mail.test");

        System.out.println("== 1. Create listings ==");
        AuctionListing guitar = svc.createListing(sam, "Vintage Guitar", "1965 hollow body", "Music",
                bd("100"), bd("10"), bd("0"), Duration.ofHours(2));
        AuctionListing watch = svc.createListing(sam, "Pocket Watch", "Silver, working", "Collectibles",
                bd("50"), bd("5"), bd("500"), Duration.ofHours(1));
        System.out.println("  " + guitar + ", " + watch);

        System.out.println("\n== 2. Search (name 'watch', price 0-100) ==");
        System.out.println("  " + svc.search(new AuctionQuery().nameContains("watch").priceBetween(bd("0"), bd("100"))));

        System.out.println("\n== 3. Bidding rules ==");
        show("Ana bids 90 (below start)", svc.placeBid(guitar.id(), ana, bd("90")));
        show("Ana bids 100", svc.placeBid(guitar.id(), ana, bd("100")));
        show("Ana bids 120 (already top)", svc.placeBid(guitar.id(), ana, bd("120")));
        show("Bob bids 105 (increment is 10)", svc.placeBid(guitar.id(), bob, bd("105")));
        show("Bob bids 110", svc.placeBid(guitar.id(), bob, bd("110")));
        show("Sam bids on own item", svc.placeBid(guitar.id(), sam, bd("500")));
        show("Ana bids 150", svc.placeBid(guitar.id(), ana, bd("150")));
        show("Bob bids 60 on the watch", svc.placeBid(watch.id(), bob, bd("60")));

        System.out.println("\n== 4. One hour later: the watch closes, reserve (500) not met ==");
        clock.advance(Duration.ofHours(1));
        svc.closeExpired().forEach(r -> System.out.println("  closed " + r.listing().id() + " reserveMet=" + r.reserveMet()));

        System.out.println("\n== 5. 50 threads bid on the guitar at once ==");
        List<User> crowd = new ArrayList<>();
        for (int i = 0; i < 50; i++) crowd.add(svc.register("bidder" + i, "b" + i + "@mail.test"));
        notifier.mute(true);                 // ~50 outbid messages would drown the output
        AtomicInteger accepted = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        for (int i = 0; i < crowd.size(); i++) {
            User u = crowd.get(i);
            BigDecimal amount = bd(String.valueOf(160 + i * 10));
            pool.submit(() -> {
                go.await();
                if (svc.placeBid(guitar.id(), u, amount).outcome() == BidOutcome.ACCEPTED) accepted.incrementAndGet();
                return null;
            });
        }
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        notifier.mute(false);
        Bid top = guitar.highestBid().orElseThrow();
        boolean increasing = true;
        List<Bid> history = guitar.bidHistory();
        for (int i = 1; i < history.size(); i++)
            increasing &= history.get(i).amount().compareTo(history.get(i - 1).amount().add(bd("10"))) >= 0;
        System.out.println("  accepted=" + accepted + " (varies run to run), highest=" + top.amount() + " by " + top.bidder().name()
                + ", history strictly increasing by >= increment: " + increasing);

        System.out.println("\n== 6. Time is up: the guitar closes and the winner is told ==");
        clock.advance(Duration.ofHours(1));
        show("late bid", svc.placeBid(guitar.id(), ana, bd("5000")));
        svc.closeExpired();
        System.out.println("  status " + guitar.status(clock.instant()) + ", final price " + guitar.currentPrice());
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private static void show(String what, BidResult r) {
        System.out.println("  " + what + " -> " + r.outcome() + " (next minimum " + r.minimumNextBid() + ")");
    }
}
