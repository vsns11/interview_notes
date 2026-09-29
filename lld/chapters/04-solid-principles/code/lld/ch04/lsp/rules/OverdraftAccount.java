package lld.ch04.lsp.rules;

import java.math.BigDecimal;

/** RULE 2 BROKEN: promises LESS -- the balance may now go negative. Nothing throws. */
public class OverdraftAccount extends Account {
    public OverdraftAccount(BigDecimal balance) {
        super(balance);
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
        return balance;
    }
}
