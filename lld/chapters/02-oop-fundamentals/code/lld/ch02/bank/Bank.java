package lld.ch02.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION: Bank uses only id(), withdraw() and deposit(). It does not know how a balance is
 * stored, what rules an account applies, or which subclass it holds.
 */
public class Bank {
    private final String name;
    private final List<Account> accounts = new ArrayList<>();

    public Bank(String name) { this.name = name; }

    public void add(Account a) { accounts.add(a); }

    public Optional<Account> find(String id) {
        return accounts.stream().filter(a -> a.id().equals(id)).findFirst();
    }

    public String transfer(String fromId, String toId, long cents) {
        Account from = find(fromId).orElseThrow();
        Account to = find(toId).orElseThrow();
        if (!from.withdraw(cents)) return fromId + " rejected, not enough money";
        to.deposit(cents);
        return "moved " + Money.format(cents) + " from " + fromId + " to " + toId;
    }

    /** POLYMORPHISM: one call, a different result per object, and no 'if' on the account type. */
    public List<String> monthEnd() {
        List<String> lines = new ArrayList<>();
        for (Account a : accounts) {
            String what = a.monthlyUpdate();                 // dynamic dispatch picks the override
            lines.add(a.id() + " (" + a.owner() + "): " + what + ", balance " + Money.format(a.balanceCents()));
        }
        return lines;
    }

    public List<Account> accounts() { return List.copyOf(accounts); }
    public String name() { return name; }
}
