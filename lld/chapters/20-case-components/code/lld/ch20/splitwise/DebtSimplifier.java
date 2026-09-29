package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Greedy "minimum cash flow": largest debtor pays largest creditor, repeat.
 * Uses at most (people - 1) payments. Not always the true minimum (that problem is NP-hard).
 */
final class DebtSimplifier {
    private record Position(String userId, BigDecimal amount) {}

    List<Payment> simplify(Ledger ledger, Collection<String> people) {
        // Biggest amount first; ties broken by name so results are deterministic.
        Comparator<Position> biggestFirst = Comparator.comparing(Position::amount).reversed()
                .thenComparing(Position::userId);
        PriorityQueue<Position> creditors = new PriorityQueue<>(biggestFirst);
        PriorityQueue<Position> debtors = new PriorityQueue<>(biggestFirst);
        for (String id : people) {
            BigDecimal net = ledger.netWithin(id, people);
            if (net.signum() > 0) creditors.add(new Position(id, net));
            else if (net.signum() < 0) debtors.add(new Position(id, net.negate()));
        }
        List<Payment> payments = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Position c = creditors.poll();
            Position d = debtors.poll();
            BigDecimal x = c.amount().min(d.amount());
            payments.add(new Payment(d.userId(), c.userId(), x));
            if (c.amount().compareTo(x) > 0) creditors.add(new Position(c.userId(), c.amount().subtract(x)));
            if (d.amount().compareTo(x) > 0) debtors.add(new Position(d.userId(), d.amount().subtract(x)));
        }
        return payments;
    }
}
