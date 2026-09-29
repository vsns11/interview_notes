package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Strategy: turns one total into one Share per participant. */
interface SplitStrategy {
    List<Share> split(BigDecimal total, List<String> participants);
}

/** Equal split. Spare cents are handed out one at a time, in participant order. */
final class EqualSplit implements SplitStrategy {
    @Override
    public List<Share> split(BigDecimal total, List<String> participants) {
        if (participants.isEmpty()) throw new IllegalArgumentException("nobody to split between");
        BigDecimal n = BigDecimal.valueOf(participants.size());
        BigDecimal base = total.divide(n, 2, RoundingMode.DOWN);           // 100.00/3 -> 33.33
        BigDecimal leftover = total.subtract(base.multiply(n));           // 0.01
        int spareCents = leftover.divide(Money.CENT).intValueExact();     // 1
        List<Share> shares = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            BigDecimal amount = i < spareCents ? base.add(Money.CENT) : base;
            shares.add(new Share(participants.get(i), amount));
        }
        return shares;
    }
}

/** Exact split: the caller states each person's amount. Expense checks the sum. */
final class ExactSplit implements SplitStrategy {
    private final Map<String, BigDecimal> amounts;

    ExactSplit(Map<String, BigDecimal> amounts) { this.amounts = Map.copyOf(amounts); }

    @Override
    public List<Share> split(BigDecimal total, List<String> participants) {
        if (!new HashSet<>(participants).equals(amounts.keySet()))
            throw new IllegalArgumentException("exact amounts must name exactly the participants");
        List<Share> shares = new ArrayList<>();
        for (String id : participants) shares.add(new Share(id, Money.normalize(amounts.get(id))));
        return shares;
    }
}

/** Percent split. Percentages must add to 100; the last person absorbs rounding. */
final class PercentSplit implements SplitStrategy {
    private final Map<String, BigDecimal> percents;

    PercentSplit(Map<String, BigDecimal> percents) { this.percents = Map.copyOf(percents); }

    @Override
    public List<Share> split(BigDecimal total, List<String> participants) {
        if (!new HashSet<>(participants).equals(percents.keySet()))
            throw new IllegalArgumentException("percentages must name exactly the participants");
        BigDecimal sum = percents.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(Money.HUNDRED) != 0)
            throw new IllegalArgumentException("percentages add up to " + sum + ", not 100");
        List<Share> shares = new ArrayList<>();
        BigDecimal given = Money.ZERO;
        for (int i = 0; i < participants.size(); i++) {
            String id = participants.get(i);
            BigDecimal amount = (i == participants.size() - 1)
                    ? total.subtract(given)                                   // absorbs the remainder
                    : total.multiply(percents.get(id)).divide(Money.HUNDRED, 2, RoundingMode.HALF_EVEN);
            given = given.add(amount);
            shares.add(new Share(id, amount));
        }
        return shares;
    }
}
