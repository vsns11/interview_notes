package lld.ch18.stackoverflow;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Notifier notifier = new Notifier(List.of(new EmailChannel()));
        StackOverflow so = new StackOverflow(new DefaultReputationPolicy(), notifier);
        so.addReputationListener(new BadgeAwarder(notifier));

        Member alice = so.register(new Member("alice", "alice@example.com"));
        Member bob = so.register(new Member("bob", "bob@example.com"));
        Member carol = so.register(new Member("carol", "carol@example.com"));
        Member erin = so.register(new Member("erin", "erin@example.com"));
        Member dave = so.register(new Member("dave", "dave@example.com"));
        Moderator mia = so.register(new Moderator("mia", "mia@example.com"));
        Admin root = so.register(new Admin("root", "root@example.com"));
        alice.seedReputation(200);
        bob.seedReputation(3500);
        carol.seedReputation(3200);
        erin.seedReputation(3100);
        LocalDate today = LocalDate.of(2026, 9, 28);

        section("1. Tags and asking");
        so.createTag(bob, "java", "The Java language");
        so.createTag(bob, "collections", "java.util collections");
        attempt(() -> so.ask(alice, "How does HashMap handle collisions?", "What happens when two keys land in the same bucket?",
                List.of("java", "hashmap")));
        Question q1 = so.ask(alice, "How does HashMap handle collisions?", "What happens when two keys land in the same bucket?",
                List.of("java", "collections"));

        section("2. Answers, votes, reputation");
        Answer bobs = so.answer(bob, q1, "Entries share a bucket as a linked list, turned into a tree when the bin grows past 8.");
        Answer carols = so.answer(carol, q1, "It uses open addressing.");
        attempt(() -> so.vote(dave, q1, VoteType.UP));               // dave has 1 rep
        attempt(() -> so.vote(alice, q1, VoteType.UP));              // own post
        so.vote(bob, q1, VoteType.UP);
        so.vote(carol, bobs, VoteType.UP);
        so.vote(erin, carols, VoteType.DOWN);
        attempt(() -> so.vote(erin, carols, VoteType.DOWN));         // double vote
        so.vote(erin, carols, VoteType.UP);                          // changing a vote reverses the old effect
        so.comment(erin, carols, "HashMap uses chaining, not open addressing.");
        attempt(() -> so.comment(dave, carols, "+1"));               // needs 50 rep to comment everywhere
        so.acceptAnswer(alice, bobs);

        section("3. Bounty");
        Question q2 = so.ask(alice, "Why is ConcurrentHashMap faster than synchronizedMap?", "Under many threads.", List.of("java"));
        so.offerBounty(alice, q2, 50, today);
        Answer carolsQ2 = so.answer(carol, q2, "Reads take no lock and writes lock only one bin.");
        so.awardBounty(alice, carolsQ2, today.plusDays(3));

        section("4. Badges for a new member");
        Question q3 = so.ask(dave, "Is Java pass-by-value?", "Objects seem to be passed by reference.", List.of("java"));
        so.vote(bob, q3, VoteType.UP);
        so.vote(carol, q3, VoteType.UP);
        so.vote(erin, q3, VoteType.UP);
        System.out.println("dave's badges: " + dave.badges().stream().map(Badge::name).toList());

        section("5. Moderation");
        so.voteToClose(bob, q3, ClosingRemark.DUPLICATE);
        so.voteToClose(carol, q3, ClosingRemark.DUPLICATE);
        so.voteToClose(erin, q3, ClosingRemark.DUPLICATE);
        attempt(() -> so.answer(bob, q3, "Yes, always by value."));
        so.reopen(mia, q3);
        Comment rude = so.comment(dave, q3, "you all are wrong!!!");
        so.flag(bob, rude, "rude");
        so.flag(carol, rude, "rude");
        so.flag(erin, rude, "rude");
        so.delete(mia, q3);
        so.undelete(mia, q3);
        so.block(root, dave);
        attempt(() -> so.ask(dave, "Another one", "text", List.of("java")));

        section("6. Guest search and top tags");
        so.search("hashmap collisions").forEach(q -> System.out.println("found: " + q.label() + " score=" + q.score()
                + " answers=" + q.answers().size() + " status=" + q.status()));
        so.searchByTag("java").forEach(q -> System.out.println("[java] " + q.label()));
        System.out.println("top tags: " + so.topTags(2).stream().map(t -> t.name() + "x" + t.uses()).toList());
        System.out.println("final reputation: " + List.of(alice, bob, carol, erin, dave));
    }

    private static void section(String title) { System.out.println("\n== " + title + " =="); }

    private static void attempt(Runnable action) {
        try { action.run(); }
        catch (RuntimeException e) { System.out.println("  REJECTED: " + e.getMessage()); }
    }
}
