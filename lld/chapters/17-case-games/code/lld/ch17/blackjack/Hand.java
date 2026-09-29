package lld.ch17.blackjack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** A blackjack hand: its cards, its bet, and how to score itself (aces = 1 or 11). */
public final class Hand {
    private final List<Card> cards = new ArrayList<>();
    private int bet;
    private boolean fromSplit;
    private boolean doubled;

    public Hand(int bet) { this.bet = bet; }

    public static Hand of(Card... cs) {
        Hand h = new Hand(0);
        for (Card c : cs) h.add(c);
        return h;
    }

    void add(Card c) { cards.add(c); }

    /** Every ace counted as 1. */
    public int hardTotal() {
        int sum = 0;
        for (Card c : cards) sum += BlackjackScoring.points(c.rank());
        return sum;
    }

    private boolean hasAce() { return cards.stream().anyMatch(c -> c.rank() == Rank.ACE); }

    /** A hand is soft when one ace can count as 11 without busting. At most one ace can ever do so. */
    public boolean isSoft() { return hasAce() && hardTotal() + 10 <= 21; }

    /** The best total: the soft total if it is legal, otherwise the hard total. */
    public int bestTotal() { return isSoft() ? hardTotal() + 10 : hardTotal(); }

    public boolean isBust() { return bestTotal() > 21; }

    /** A natural: exactly two cards worth 21, and not the result of a split. */
    public boolean isBlackjack() { return !fromSplit && cards.size() == 2 && bestTotal() == 21; }

    /** Two cards of the same rank, and no re-splitting (one split per round). */
    public boolean canSplit() {
        return !fromSplit && cards.size() == 2 && cards.get(0).rank() == cards.get(1).rank();
    }

    /** Takes the second card out to start a new hand; both hands are now split hands. */
    Hand split() {
        if (!canSplit()) throw new IllegalStateException("hand cannot be split");
        Hand other = new Hand(bet);
        other.add(cards.remove(1));
        this.fromSplit = true;
        other.fromSplit = true;
        return other;
    }

    void doubleDown() { bet *= 2; doubled = true; }

    public boolean isDoubled() { return doubled; }
    public int bet() { return bet; }
    public int size() { return cards.size(); }
    public Card first() { return cards.get(0); }
    public List<Card> cards() { return List.copyOf(cards); }

    @Override public String toString() {
        String cs = cards.stream().map(Card::toString).collect(Collectors.joining(" "));
        String kind = isBlackjack() ? "BLACKJACK" : isBust() ? "bust " + bestTotal()
                : (isSoft() ? "soft " : "") + bestTotal();
        return "[" + cs + "] = " + kind;
    }
}
