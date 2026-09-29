package lld.ch17.blackjack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * A casino shoe: several standard decks shuffled together. A "cut card" marks how deep we deal
 * before reshuffling (penetration). A stacked shoe (fixed order) exists for deterministic tests.
 */
public final class Shoe {
    private final int numberOfDecks;
    private final Random rng;          // null for a stacked test shoe
    private final int cutCardPosition; // reshuffle when fewer cards than this remain
    private final Deque<Card> cards = new ArrayDeque<>();

    public Shoe(int numberOfDecks, Random rng, double penetration) {
        if (numberOfDecks < 1) throw new IllegalArgumentException("need at least one deck");
        if (penetration <= 0 || penetration >= 1) throw new IllegalArgumentException("penetration must be in (0,1)");
        this.numberOfDecks = numberOfDecks;
        this.rng = rng;
        this.cutCardPosition = (int) Math.round(numberOfDecks * 52 * (1 - penetration));
        refill();
    }

    private Shoe(List<Card> stackedOrder) {
        this.numberOfDecks = 0;
        this.rng = null;
        this.cutCardPosition = 0;
        cards.addAll(stackedOrder);
    }

    /** Cards come out in exactly the order given. Perfect for unit tests. */
    public static Shoe stacked(Card... order) { return new Shoe(List.of(order)); }

    public Card deal() {
        if (cards.isEmpty()) {
            if (rng == null) throw new IllegalStateException("stacked shoe ran out of cards");
            refill();
        }
        return cards.pop();
    }

    public boolean needsReshuffle() { return rng != null && cards.size() < cutCardPosition; }

    public void reshuffle() { refill(); }

    public int remaining() { return cards.size(); }

    private void refill() {
        List<Card> all = new ArrayList<>(numberOfDecks * 52);
        for (int i = 0; i < numberOfDecks; i++) {
            all.addAll(Deck.standard52().cards());
        }
        Collections.shuffle(all, rng);
        cards.clear();
        cards.addAll(all);
    }
}
