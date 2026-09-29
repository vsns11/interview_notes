package lld.ch17.blackjack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A generic deck: any card type T (poker card, Uno card, tarot card...).
 * Shuffling takes a Random so that tests can pass a seeded one and get the same order every run.
 */
public final class Deck<T> {
    private final Deque<T> cards;

    public Deck(Collection<? extends T> cards) { this.cards = new ArrayDeque<>(cards); }

    /** Factory for the standard 52-card French deck (13 ranks x 4 suits). */
    public static Deck<Card> standard52() {
        List<Card> all = new ArrayList<>(52);
        for (Suit s : Suit.values()) {
            for (Rank r : Rank.values()) {
                all.add(new Card(r, s));
            }
        }
        return new Deck<>(all);
    }

    public void shuffle(Random rng) {
        List<T> list = new ArrayList<>(cards);
        Collections.shuffle(list, rng);
        cards.clear();
        cards.addAll(list);
    }

    public T draw() {
        if (cards.isEmpty()) throw new NoSuchElementException("deck is empty");
        return cards.pop();
    }

    public int size() { return cards.size(); }

    public List<T> cards() { return List.copyOf(cards); }
}
