package lld.ch17.blackjack;

/** Thirteen ranks with their face value (ace = 1, jack = 11, queen = 12, king = 13). */
public enum Rank {
    ACE("A", 1), TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5), SIX("6", 6),
    SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9), TEN("10", 10),
    JACK("J", 11), QUEEN("Q", 12), KING("K", 13);

    private final String symbol;
    private final int faceValue;

    Rank(String symbol, int faceValue) { this.symbol = symbol; this.faceValue = faceValue; }

    public String symbol() { return symbol; }
    public int faceValue() { return faceValue; }
}
