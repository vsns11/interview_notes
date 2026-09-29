package lld.ch17.blackjack;

/**
 * Strategy pattern: HOW a player decides is pluggable (human via UI, basic-strategy bot, test script).
 * The game passes what is allowed, so a strategy never has to know the betting rules.
 */
public interface PlayerStrategy {
    Action decide(Hand hand, Card dealerUpCard, boolean canDouble, boolean canSplit);

    /** Offered only when the dealer shows an ace. Mathematically a bad bet for most players. */
    default boolean takeInsurance(Hand hand) { return false; }
}
