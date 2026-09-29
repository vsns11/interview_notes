package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Who owes whom. Exactly ONE signed number per unordered pair of people.
 * Key (lo, hi) with lo < hi alphabetically; a positive value means "hi owes lo".
 */
final class Ledger {
    private record Pair(String lo, String hi) {
        static Pair of(String a, String b) { return a.compareTo(b) < 0 ? new Pair(a, b) : new Pair(b, a); }
    }

    private final Map<Pair, BigDecimal> balances = new TreeMap<>(
            (p, q) -> p.lo().equals(q.lo()) ? p.hi().compareTo(q.hi()) : p.lo().compareTo(q.lo()));

    /** The ONLY mutator: 'debtor' now owes 'creditor' 'amount' more. Opposite debts cancel. */
    void adjust(String debtor, String creditor, BigDecimal amount) {
        if (debtor.equals(creditor) || amount.signum() == 0) return;
        Pair key = Pair.of(debtor, creditor);
        BigDecimal delta = creditor.equals(key.lo()) ? amount : amount.negate();
        BigDecimal updated = balances.getOrDefault(key, Money.ZERO).add(delta);
        if (updated.signum() == 0) balances.remove(key); else balances.put(key, updated);
    }

    /** Drops every debt where both people are in 'circle' (used when a simplified plan replaces them). */
    void clearWithin(Collection<String> circle) {
        balances.keySet().removeIf(p -> circle.contains(p.lo()) && circle.contains(p.hi()));
    }

    /** How much 'debtor' owes 'creditor' (never negative). */
    BigDecimal owes(String debtor, String creditor) {
        if (debtor.equals(creditor)) return Money.ZERO;
        Pair key = Pair.of(debtor, creditor);
        BigDecimal v = balances.getOrDefault(key, Money.ZERO);
        BigDecimal debtorOwes = creditor.equals(key.lo()) ? v : v.negate();
        return debtorOwes.signum() > 0 ? debtorOwes : Money.ZERO;
    }

    /** Positive = others owe this person; negative = this person owes others. */
    BigDecimal net(String userId) { return netWithin(userId, null); }

    /** Net position counting only debts with people in 'circle' (null = everybody). */
    BigDecimal netWithin(String userId, Collection<String> circle) {
        BigDecimal net = Money.ZERO;
        for (var e : balances.entrySet()) {
            Pair p = e.getKey();
            String other = p.lo().equals(userId) ? p.hi() : p.hi().equals(userId) ? p.lo() : null;
            if (other == null || (circle != null && !circle.contains(other))) continue;
            // positive value: hi owes lo, so lo is up and hi is down
            net = p.lo().equals(userId) ? net.add(e.getValue()) : net.subtract(e.getValue());
        }
        return net;
    }

    /** Human-readable list of every open debt: "B owes A 10.00". */
    void print() {
        if (balances.isEmpty()) System.out.println("  (everyone is settled up)");
        balances.forEach((p, v) -> {
            if (v.signum() > 0) System.out.println("  " + p.hi() + " owes " + p.lo() + " " + v);
            else System.out.println("  " + p.lo() + " owes " + p.hi() + " " + v.negate());
        });
    }
}
