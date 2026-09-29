package lld.ch17.blackjack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Runs rounds at one table: bets, deal, insurance + dealer peek, player turns (hit/stand/double/split),
 * dealer turn, settlement. Each player plays only against the dealer.
 */
public final class BlackjackGame {
    private final Shoe shoe;
    private final DealerPolicy dealerPolicy;
    private final List<Player> players;
    private final Consumer<String> log;

    public BlackjackGame(Shoe shoe, DealerPolicy dealerPolicy, List<Player> players, Consumer<String> log) {
        this.shoe = shoe;
        this.dealerPolicy = dealerPolicy;
        this.players = List.copyOf(players);
        this.log = log;
    }

    public void playRound(int ante) {
        if (ante <= 0 || ante % 2 != 0) throw new IllegalArgumentException("ante must be a positive even number");
        if (shoe.needsReshuffle()) {
            shoe.reshuffle();
            log.accept("  (cut card reached: shoe reshuffled)");
        }

        // 1. Bets: each player who can afford the ante gets one hand.
        Map<Player, List<Hand>> hands = new LinkedHashMap<>();
        for (Player p : players) {
            if (!p.canAfford(ante)) { log.accept("  " + p.name() + " sits out (balance " + p.balance() + ")"); continue; }
            p.debit(ante);
            hands.put(p, new ArrayList<>(List.of(new Hand(ante))));
        }
        if (hands.isEmpty()) return;

        // 2. Deal two rounds: one card to each player, then one to the dealer. Dealer's 2nd card is the hole card.
        Hand dealer = new Hand(0);
        for (int round = 0; round < 2; round++) {
            for (List<Hand> hs : hands.values()) hs.get(0).add(shoe.deal());
            dealer.add(shoe.deal());
        }
        Card up = dealer.first();
        log.accept("  Dealer shows " + up + " (hole card hidden)");
        hands.forEach((p, hs) -> log.accept("  " + p.name() + " " + hs.get(0)));

        // 3. Insurance (only when the up card is an ace), then the dealer peeks for blackjack.
        Map<Player, Integer> insurance = new LinkedHashMap<>();
        if (up.rank() == Rank.ACE) {
            hands.forEach((p, hs) -> {
                int side = ante / 2;
                if (p.strategy().takeInsurance(hs.get(0)) && p.canAfford(side)) {
                    p.debit(side);
                    insurance.put(p, side);
                    log.accept("  " + p.name() + " buys insurance for " + side);
                }
            });
        }
        boolean dealerMayHaveBlackjack = up.rank() == Rank.ACE || BlackjackScoring.points(up.rank()) == 10;
        if (dealerMayHaveBlackjack && dealer.isBlackjack()) {
            log.accept("  Dealer peeks: " + dealer + " -> round over");
            insurance.forEach((p, side) -> { p.credit(side * 3); log.accept("  " + p.name() + " insurance pays 2:1 (+" + side * 2 + ")"); });
            hands.forEach((p, hs) -> {
                Hand h = hs.get(0);
                if (h.isBlackjack()) { p.credit(h.bet()); log.accept("  " + p.name() + ": PUSH (both blackjack)"); }
                else log.accept("  " + p.name() + ": LOSE " + h.bet());
            });
            printBalances();
            return;
        }
        if (!insurance.isEmpty()) log.accept("  Dealer peeks: no blackjack, insurance bets lost");

        // 4. Player turns. Splitting inserts a new hand right after the current one.
        hands.forEach((p, hs) -> {
            for (int i = 0; i < hs.size(); i++) playHand(p, hs, i, up);
        });

        // 5. Dealer turn (skipped if every player hand is already bust).
        boolean anyLive = hands.values().stream().flatMap(List::stream).anyMatch(h -> !h.isBust());
        if (anyLive) {
            while (dealerPolicy.shouldHit(dealer)) dealer.add(shoe.deal());
            log.accept("  Dealer reveals and plays: " + dealer);
        }

        // 6. Settle every hand against the dealer.
        hands.forEach((p, hs) -> hs.forEach(h -> settle(p, h, dealer)));
        printBalances();
    }

    private void playHand(Player p, List<Hand> hs, int index, Card up) {
        Hand hand = hs.get(index);
        String label = p.name() + (hs.size() > 1 ? " hand " + (index + 1) : "");
        while (!hand.isBlackjack() && hand.bestTotal() < 21) {
            boolean canDouble = hand.size() == 2 && p.canAfford(hand.bet());
            boolean canSplit = hand.canSplit() && p.canAfford(hand.bet());
            Action a = p.strategy().decide(hand, up, canDouble, canSplit);
            if ((a == Action.DOUBLE && !canDouble) || (a == Action.SPLIT && !canSplit)) {
                throw new IllegalStateException(label + " chose a move that is not allowed: " + a);
            }
            switch (a) {
                case STAND -> { log.accept("  " + label + " stands on " + hand.bestTotal()); return; }
                case HIT -> { hand.add(shoe.deal()); log.accept("  " + label + " hits: " + hand); }
                case DOUBLE -> {
                    p.debit(hand.bet());
                    hand.doubleDown();
                    hand.add(shoe.deal());
                    log.accept("  " + label + " doubles (bet now " + hand.bet() + "): " + hand);
                    return;                                     // exactly one card after doubling
                }
                case SPLIT -> {
                    p.debit(hand.bet());
                    Hand second = hand.split();
                    hand.add(shoe.deal());
                    second.add(shoe.deal());
                    hs.add(index + 1, second);
                    label = p.name() + " hand " + (index + 1);
                    log.accept("  " + p.name() + " splits -> " + hand + " and " + second);
                }
            }
        }
        if (hand.isBust()) log.accept("  " + label + " busts");
    }

    private void settle(Player p, Hand h, Hand dealer) {
        String who = "  " + p.name() + " " + h + " vs dealer " + dealer.bestTotal() + ": ";
        if (h.isBust()) { log.accept(who + "LOSE " + h.bet()); return; }
        if (h.isBlackjack()) {                                  // dealer blackjack was handled by the peek
            p.credit(h.bet() + h.bet() * 3 / 2);
            log.accept(who + "BLACKJACK pays 3:2 (+" + h.bet() * 3 / 2 + ")");
            return;
        }
        int mine = h.bestTotal(), theirs = dealer.bestTotal();
        if (dealer.isBust() || mine > theirs) { p.credit(h.bet() * 2); log.accept(who + "WIN +" + h.bet()); }
        else if (mine == theirs) { p.credit(h.bet()); log.accept(who + "PUSH"); }
        else log.accept(who + "LOSE " + h.bet());
    }

    private void printBalances() {
        StringBuilder sb = new StringBuilder("  Balances:");
        players.forEach(p -> sb.append(' ').append(p.name()).append('=').append(p.balance()));
        log.accept(sb.toString());
    }
}
