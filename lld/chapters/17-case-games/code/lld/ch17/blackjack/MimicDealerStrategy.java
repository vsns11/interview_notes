package lld.ch17.blackjack;

/** A naive player who copies the dealer (hit below 17) and always buys insurance. */
public final class MimicDealerStrategy implements PlayerStrategy {
    @Override
    public Action decide(Hand hand, Card up, boolean canDouble, boolean canSplit) {
        return hand.bestTotal() < 17 ? Action.HIT : Action.STAND;
    }

    @Override public boolean takeInsurance(Hand hand) { return true; }
}
