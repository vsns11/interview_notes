package lld.ch17.blackjack;

/** A simplified version of the well-known "basic strategy" table. */
public final class BasicStrategy implements PlayerStrategy {
    @Override
    public Action decide(Hand hand, Card up, boolean canDouble, boolean canSplit) {
        int dealer = BlackjackScoring.points(up.rank()) == 1 ? 11 : BlackjackScoring.points(up.rank());
        if (canSplit && (hand.first().rank() == Rank.ACE || hand.first().rank() == Rank.EIGHT)) {
            return Action.SPLIT;                                   // always split aces and eights
        }
        int total = hand.bestTotal();
        if (hand.isSoft()) {
            return total >= 18 ? Action.STAND : Action.HIT;
        }
        if (canDouble && (total == 11 || (total == 10 && dealer <= 9))) {
            return Action.DOUBLE;
        }
        if (total >= 17) return Action.STAND;
        if (total >= 13) return dealer <= 6 ? Action.STAND : Action.HIT;
        if (total == 12) return (dealer >= 4 && dealer <= 6) ? Action.STAND : Action.HIT;
        return Action.HIT;
    }
}
