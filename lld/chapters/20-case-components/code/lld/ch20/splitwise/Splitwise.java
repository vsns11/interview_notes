package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Facade the app talks to. Orders the steps; the arithmetic lives in the other classes. */
final class Splitwise {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Expense> expenses = new LinkedHashMap<>();
    private final Ledger ledger = new Ledger();
    private final DebtSimplifier simplifier = new DebtSimplifier();
    private final AtomicInteger ids = new AtomicInteger();

    synchronized User addUser(String name, String email) {
        User u = new User(name.toLowerCase(), name, email);
        users.put(u.id(), u);
        return u;
    }

    synchronized Group createGroup(String name, String... memberIds) {
        Group g = new Group("g" + ids.incrementAndGet(), name);
        for (String m : memberIds) g.addMember(requireUser(m));
        groups.put(g.id(), g);
        return g;
    }

    /** Build shares -> build (and validate) expense -> only then touch the ledger. */
    synchronized Expense addExpense(String groupId, String description, String paidBy, BigDecimal total,
                                    List<String> participants, SplitStrategy strategy) {
        requireUser(paidBy);
        participants.forEach(this::requireUser);
        Group group = groupId == null ? null : groups.get(groupId);
        if (group != null) {
            for (String p : participants)
                if (!group.hasMember(p)) throw new IllegalArgumentException(p + " is not in " + group.name());
            if (!group.hasMember(paidBy)) throw new IllegalArgumentException(paidBy + " is not in " + group.name());
        }
        List<Share> shares = strategy.split(total, participants);       // may throw
        Expense e = Expense.create("e" + ids.incrementAndGet(), description, paidBy, total, shares, groupId);
        for (Share s : e.shares()) ledger.adjust(s.userId(), paidBy, s.amount()); // payer's own share is a no-op
        expenses.put(e.id(), e);
        if (group != null) group.recordExpense(e.id());
        return e;
    }

    /** A real payment from 'from' to 'to'. Cannot pay more than is owed. */
    synchronized void settleUp(String from, String to, BigDecimal amount) {
        BigDecimal owed = ledger.owes(from, to);
        if (amount.signum() <= 0 || amount.compareTo(owed) > 0)
            throw new IllegalArgumentException(from + " owes " + to + " only " + owed + ", cannot pay " + amount);
        ledger.adjust(to, from, amount); // the reverse debt cancels the existing one
    }

    synchronized BigDecimal owes(String debtor, String creditor) { return ledger.owes(debtor, creditor); }
    synchronized BigDecimal net(String userId) { return ledger.net(userId); }
    synchronized List<Payment> simplify(String groupId) {
        return simplifier.simplify(ledger, groups.get(groupId).members());
    }
    /** Replaces the group's tangle of debts with the simplified plan. Every net position is unchanged. */
    synchronized List<Payment> simplifyAndApply(String groupId) {
        var members = groups.get(groupId).members();
        List<Payment> plan = simplifier.simplify(ledger, members);
        ledger.clearWithin(members);
        for (Payment p : plan) ledger.adjust(p.from(), p.to(), p.amount());
        return plan;
    }

    synchronized void printLedger() { ledger.print(); }

    synchronized List<String> history(String groupId) {
        List<String> lines = new ArrayList<>();
        for (String id : groups.get(groupId).expenseIds()) {
            Expense e = expenses.get(id);
            lines.add(e.id() + " " + e.description() + " " + e.total() + " paid by " + e.paidBy() + " " + e.shares());
        }
        return lines;
    }

    private String requireUser(String id) {
        if (!users.containsKey(id)) throw new IllegalArgumentException("unknown user " + id);
        return id;
    }
}
