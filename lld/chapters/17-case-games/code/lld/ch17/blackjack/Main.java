package lld.ch17.blackjack;

import java.util.List;
import java.util.Random;

public final class Main {
    private static Card c(Rank r, Suit s) { return new Card(r, s); }

    public static void main(String[] args) {
        System.out.println("== 1. Hand values (aces are 1 or 11) ==");
        List<Hand> samples = List.of(
                Hand.of(c(Rank.ACE, Suit.SPADES), c(Rank.SIX, Suit.HEARTS)),
                Hand.of(c(Rank.ACE, Suit.SPADES), c(Rank.SIX, Suit.HEARTS), c(Rank.TEN, Suit.CLUBS)),
                Hand.of(c(Rank.ACE, Suit.SPADES), c(Rank.ACE, Suit.HEARTS), c(Rank.NINE, Suit.CLUBS)),
                Hand.of(c(Rank.KING, Suit.SPADES), c(Rank.QUEEN, Suit.HEARTS), c(Rank.FIVE, Suit.CLUBS)),
                Hand.of(c(Rank.ACE, Suit.DIAMONDS), c(Rank.KING, Suit.CLUBS)));
        samples.forEach(h -> System.out.println("  " + h + "   (hard total " + h.hardTotal() + ")"));

        System.out.println("\n== 2. Stacked shoe: split 8s, then double both hands ==");
        Shoe stacked = Shoe.stacked(
                c(Rank.EIGHT, Suit.HEARTS), c(Rank.SIX, Suit.SPADES),     // Alice 1st card, dealer up card
                c(Rank.EIGHT, Suit.DIAMONDS), c(Rank.TEN, Suit.CLUBS),    // Alice 2nd card, dealer hole card
                c(Rank.THREE, Suit.CLUBS), c(Rank.TWO, Suit.SPADES),      // one card to each split hand
                c(Rank.NINE, Suit.HEARTS), c(Rank.FIVE, Suit.DIAMONDS),   // the two double-down cards
                c(Rank.SEVEN, Suit.SPADES));                              // dealer's hit on 16
        Player alice = new Player("Alice", 100, new BasicStrategy());
        new BlackjackGame(stacked, DealerPolicy.standOnAll17(), List.of(alice), System.out::println).playRound(10);

        System.out.println("\n== 3. Stacked shoe: dealer shows an ace, Bob buys insurance ==");
        Shoe aceUp = Shoe.stacked(c(Rank.TEN, Suit.HEARTS), c(Rank.ACE, Suit.SPADES),
                c(Rank.NINE, Suit.CLUBS), c(Rank.KING, Suit.DIAMONDS));
        Player bob = new Player("Bob", 100, new MimicDealerStrategy());
        new BlackjackGame(aceUp, DealerPolicy.standOnAll17(), List.of(bob), System.out::println).playRound(10);

        System.out.println("\n== 4. Seeded 6-deck shoe (seed 42), two players, 5 rounds ==");
        Shoe shoe = new Shoe(6, new Random(42), 0.75);
        Player carol = new Player("Carol", 100, new BasicStrategy());
        Player dave = new Player("Dave", 100, new MimicDealerStrategy());
        BlackjackGame game = new BlackjackGame(shoe, DealerPolicy.hitSoft17(), List.of(carol, dave), System.out::println);
        for (int round = 1; round <= 5; round++) {
            System.out.println("Round " + round + ":");
            game.playRound(10);
        }
    }
}
