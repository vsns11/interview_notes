package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** What was bought, what it cost, who paid, and each share. Validated once, at creation. */
final class Expense {
    private final String id;
    private final String description;
    private final String paidBy;
    private final BigDecimal total;
    private final List<Share> shares;
    private final String groupId; // null for an expense between friends outside any group

    private Expense(String id, String description, String paidBy, BigDecimal total,
                    List<Share> shares, String groupId) {
        this.id = id;
        this.description = description;
        this.paidBy = paidBy;
        this.total = total;
        this.shares = List.copyOf(shares);
        this.groupId = groupId;
    }

    /** Factory: builds a valid expense or throws with the reason it is invalid. */
    static Expense create(String id, String description, String paidBy, BigDecimal total,
                          List<Share> shares, String groupId) {
        problem(paidBy, total, shares).ifPresent(p -> { throw new IllegalArgumentException(p); });
        return new Expense(id, description, paidBy, total, shares, groupId);
    }

    /** Why these shares cannot form an expense, or empty if they can. */
    static Optional<String> problem(String paidBy, BigDecimal total, List<Share> shares) {
        if (total.signum() <= 0) return Optional.of("total must be positive");
        Set<String> sharers = new HashSet<>();
        for (Share s : shares) {
            if (s.amount().signum() < 0) return Optional.of("negative share for " + s.userId());
            if (!sharers.add(s.userId())) return Optional.of("duplicate share for " + s.userId());
        }
        Set<String> people = new HashSet<>(sharers);
        people.add(paidBy); // the payer counts as one of the two people even if they owe no share
        if (people.size() < 2) return Optional.of("an expense needs at least two people");
        BigDecimal sum = shares.stream().map(Share::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(total) != 0)
            return Optional.of("shares add up to " + sum + " but the total is " + total);
        return Optional.empty();
    }

    String id() { return id; }
    String description() { return description; }
    String paidBy() { return paidBy; }
    BigDecimal total() { return total; }
    List<Share> shares() { return shares; }
    String groupId() { return groupId; }
}
