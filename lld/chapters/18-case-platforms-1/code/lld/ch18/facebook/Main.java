package lld.ch18.facebook;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        NotificationChannel push = new PushChannel();
        NotificationChannel email = new EmailChannel();
        Facebook fb = new Facebook(Map.of(
                NotificationType.FRIEND_REQUEST, List.of(push, email),
                NotificationType.NEW_MESSAGE, List.of(push),
                NotificationType.NEW_COMMENT, List.of(push)));

        Member alice = fb.register(new Member("Alice Rao", "alice@example.com"));
        Member bob = fb.register(new Member("Bob Das", "bob@example.com"));
        Member carol = fb.register(new Member("Carol Nair", "carol@example.com"));
        Member dave = fb.register(new Member("Dave Paul", "dave@example.com"));
        Member erin = fb.register(new Member("Erin Shah", "erin@example.com"));
        Member frank = fb.register(new Member("Frank Lee", "frank@example.com"));
        Member grace = fb.register(new Member("Grace Lee", "grace@example.com"));
        Admin admin = fb.register(new Admin("Admin", "admin@example.com"));

        section("1. Profile and search by name");
        alice.profile().addWork(new Work("Designer", "Studio Nine", "Mumbai", 2021, null));
        alice.profile().addPlace(new Place("Mumbai", "India", 2019));
        System.out.println("Alice's picture before upload: " + alice.profile().picture());
        System.out.println("search 'lee': " + fb.search.searchMembers("lee"));

        section("2. Friend requests");
        FriendRequest r = fb.friends.sendRequest(alice, bob);
        fb.friends.accept(r, bob);
        attempt(() -> fb.friends.accept(r, bob));
        befriend(fb, alice, carol);
        befriend(fb, alice, erin);
        befriend(fb, bob, dave);
        befriend(fb, carol, dave);
        befriend(fb, erin, grace);
        frank.follow(alice);                                         // follow without being friends

        section("3. Friend suggestions (extended requirement)");
        System.out.println("before: " + fb.suggestions.suggest(alice, 5));
        fb.friends.sendRequest(alice, grace);
        System.out.println("after inviting Grace: " + fb.suggestions.suggest(alice, 5));
        attempt(() -> fb.friends.sendRequest(grace, alice));        // pending the other way

        section("4. Posts and privacy");
        PrivacyList close = fb.createPrivacyList(alice, "Close friends", List.of(bob, carol));
        attempt(() -> fb.createPrivacyList(alice, "Oops", List.of(frank)));
        Post pub = fb.post(alice, "New portfolio is live!", new Audience.Public());
        Post fr = fb.post(alice, "Moving to Pune next month", new Audience.Friends());
        Post secret = fb.post(alice, "Planning a surprise party for Erin", new Audience.Custom(close));
        for (Member viewer : List.of(alice, bob, erin, frank)) {
            System.out.printf("  %-10s public=%-5s friends=%-5s closeFriends=%s%n", viewer.name(),
                    fb.visibility.canSee(viewer, pub), fb.visibility.canSee(viewer, fr), fb.visibility.canSee(viewer, secret));
        }
        System.out.println("search posts 'party' as Bob:  " + fb.search.searchPosts("party", bob));
        System.out.println("search posts 'party' as Erin: " + fb.search.searchPosts("party", erin));

        section("5. Comments, likes, shares");
        Comment cm = fb.comment(bob, secret, "I'll bring the cake");
        fb.like(carol, secret, secret);
        fb.like(alice, cm, secret);                                  // comments can be liked too
        System.out.println("Bob likes: " + fb.like(bob, secret, secret) + " / again: " + fb.like(bob, secret, secret));
        attempt(() -> fb.comment(erin, secret, "What party?"));
        fb.share(erin, pub, new Audience.Friends());

        section("6. Pages and groups");
        Page pizza = fb.createPage(carol, "Joe's Pizza", PageType.BUSINESS);
        fb.followPage(alice, pizza);
        pizza.recommend(alice, 5, "Best crust in town");
        pizza.recommend(bob, 4, "Good, a bit slow");
        attempt(() -> pizza.recommend(alice, 1, "changed my mind"));
        fb.publishOnPage(carol, pizza, "Two-for-one pizza on Friday");
        System.out.printf("%s: followers=%d, rating=%.1f from %d recommendations%n",
                pizza.name(), pizza.followerCount(), pizza.averageRating().orElse(0), pizza.recommendationCount());
        Group chess = fb.createGroup(dave, "Chess Club", GroupPrivacy.PRIVATE);
        System.out.println("Frank joined at once? " + chess.requestToJoin(frank));
        chess.approve(dave, frank);
        fb.postInGroup(frank, chess, "Chess tournament on Sunday");
        attempt(() -> fb.postInGroup(alice, chess, "Can I play?"));
        System.out.println("search posts 'chess' as Alice: " + fb.search.searchPosts("chess", alice)
                + ", as Dave: " + fb.search.searchPosts("chess", dave));

        section("7. News feeds");
        System.out.println("Alice (recent): " + fb.feed.feed(alice, new RecentFirst(), 5));
        System.out.println("Bob   (ranked): " + fb.feed.feed(bob, new EngagementRanker(fb.friends), 5));
        System.out.println("Frank (recent): " + fb.feed.feed(frank, new RecentFirst(), 5));

        section("8. Messages and admin");
        System.out.println("Dave before Frank is blocked: " + fb.feed.feed(dave, new RecentFirst(), 5));
        fb.message(bob, List.of(alice, carol), "Party at 8?");
        fb.block(admin, frank);
        System.out.println("Dave after Frank is blocked: " + fb.feed.feed(dave, new RecentFirst(), 5));
        System.out.println("Alice's notifications: " + alice.notifications().stream().map(Notification::type).toList());
    }

    static void befriend(Facebook fb, Member a, Member b) { fb.friends.accept(fb.friends.sendRequest(a, b), b); }

    static void section(String t) { System.out.println("\n== " + t + " =="); }

    static void attempt(Runnable r) {
        try { r.run(); } catch (RuntimeException e) { System.out.println("  REJECTED: " + e.getMessage()); }
    }
}
