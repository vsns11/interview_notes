package lld.ch04.lsp.rules;

import java.math.BigDecimal;

/** Contract -- postcondition of withdraw: the balance is never negative. */
public class Account {
    protected BigDecimal balance;

    public Account(BigDecimal balance) {
        this.balance = balance;
    }

    /** Withdraws only if funds allow; returns the balance afterwards (always >= 0). */
    public BigDecimal withdraw(BigDecimal amount) {
        if (amount.compareTo(balance) <= 0) {
            balance = balance.subtract(amount);
        }
        return balance;
    }
}
