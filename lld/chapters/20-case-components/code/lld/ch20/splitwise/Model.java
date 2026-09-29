package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Money helpers: every amount is a BigDecimal with exactly 2 decimal places. */
final class Money {
    static final BigDecimal ZERO = new BigDecimal("0.00");
    static final BigDecimal CENT = new BigDecimal("0.01");
    static final BigDecimal HUNDRED = new BigDecimal("100");

    private Money() {}

    static BigDecimal of(String amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.UNNECESSARY); // refuses 10.005
    }

    static BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}

/** A person. Identity only - no money lives here. */
record User(String id, String name, String email) {
    User {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
    }
}

/** A named set of people. Holds member ids and expense ids, never balances. */
final class Group {
    private final String id;
    private final String name;
    private final Set<String> memberIds = new LinkedHashSet<>();
    private final List<String> expenseIds = new ArrayList<>();

    Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    String id() { return id; }
    String name() { return name; }
    void addMember(String userId) { memberIds.add(userId); }
    boolean hasMember(String userId) { return memberIds.contains(userId); }
    Set<String> members() { return Collections.unmodifiableSet(memberIds); }
    void recordExpense(String expenseId) { expenseIds.add(expenseId); }
    List<String> expenseIds() { return Collections.unmodifiableList(expenseIds); }
}

/** One person's part of one expense (a value object). */
record Share(String userId, BigDecimal amount) {
    @Override
    public String toString() { return userId + "=" + amount; }
}

/** A suggested transfer produced by the simplifier - a suggestion, not a record. */
record Payment(String from, String to, BigDecimal amount) {
    @Override
    public String toString() {
        return from + " pays " + to + " " + amount;
    }
}
