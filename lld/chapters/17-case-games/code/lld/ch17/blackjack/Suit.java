package lld.ch17.blackjack;

/** The four suits. A suit never affects a blackjack score, but a generic deck needs it. */
public enum Suit {
    CLUBS('C'), DIAMONDS('D'), HEARTS('H'), SPADES('S');

    private final char letter;

    Suit(char letter) { this.letter = letter; }

    public char letter() { return letter; }
}
