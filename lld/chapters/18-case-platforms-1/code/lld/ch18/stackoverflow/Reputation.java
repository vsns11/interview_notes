package lld.ch18.stackoverflow;

import java.util.List;

/**
 * Strategy: the scoring rules live behind an interface, so voting code does not
 * know the numbers. Changing a rule (e.g. the 2019 move of question upvotes
 * from +5 to +10) is a new
 * policy, not an edit to the voting code (Open/Closed Principle).
 */
interface ReputationPolicy {
    int authorDeltaForVote(Post post, VoteType type);
    int voterDeltaForVote(Post post, VoteType type);
    int acceptedAnswerBonus();
    int acceptorBonus();
}

/** Today's stackoverflow.com rules: +10 for any upvote, -2 to the author per downvote, -1 to whoever downvotes an answer. */
final class DefaultReputationPolicy implements ReputationPolicy {
    @Override public int authorDeltaForVote(Post post, VoteType type) {
        return switch (post) {
            case Question q -> type == VoteType.UP ? 10 : -2;   // +5 before November 2019
            case Answer a -> type == VoteType.UP ? 10 : -2;
            case Comment c -> 0;                       // comment votes earn no reputation
        };
    }
    @Override public int voterDeltaForVote(Post post, VoteType type) {
        return post instanceof Answer && type == VoteType.DOWN ? -1 : 0;
    }
    @Override public int acceptedAnswerBonus() { return 15; }
    @Override public int acceptorBonus() { return 2; }
}

/** Observer: anything that wants to react to a reputation change. */
interface ReputationListener {
    void onReputationChanged(Member member, int oldValue, int newValue, String reason);
}

/** Awards reputation-level badges (course thresholds: 15, 50, 125). */
final class BadgeAwarder implements ReputationListener {
    private record Level(int reputation, Badge badge) {}

    private final List<Level> levels = List.of(
            new Level(15, new Badge("Contributor", "reached 15 reputation")),
            new Level(50, new Badge("Helpful", "reached 50 reputation")),
            new Level(125, new Badge("Trusted", "reached 125 reputation")));
    private final Notifier notifier;

    BadgeAwarder(Notifier notifier) { this.notifier = notifier; }

    @Override public void onReputationChanged(Member m, int oldValue, int newValue, String reason) {
        for (Level level : levels) {
            if (oldValue < level.reputation() && newValue >= level.reputation() && m.addBadge(level.badge())) {
                notifier.send(m, "You earned the '" + level.badge().name() + "' badge");
            }
        }
    }
}

/** Delivery channel for notifications (Strategy: email today, push tomorrow). */
interface NotificationChannel {
    void deliver(Member to, String text);
}

final class EmailChannel implements NotificationChannel {
    @Override public void deliver(Member to, String text) {
        System.out.println("    [email to " + to.email() + "] " + text);
    }
}

/** The "System" actor: sends notifications through every configured channel. */
final class Notifier {
    private final List<NotificationChannel> channels;
    Notifier(List<NotificationChannel> channels) { this.channels = List.copyOf(channels); }
    void send(Member to, String text) { channels.forEach(c -> c.deliver(to, text)); }
}
