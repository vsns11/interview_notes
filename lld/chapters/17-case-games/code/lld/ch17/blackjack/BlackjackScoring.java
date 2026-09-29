package lld.ch17.blackjack;

/**
 * The blackjack-specific value of a card lives here, NOT on Card.
 * A king has face value 13 in general, but it is worth 10 points in this game.
 */
public final class BlackjackScoring {
    private BlackjackScoring() {}

    /** Points with an ace counted as 1 (the "hard" value). */
    public static int points(Rank rank) {
        return switch (rank) {
            case ACE -> 1;
            case JACK, QUEEN, KING -> 10;
            default -> rank.faceValue();
        };
    }
}
