package lld.ch21.ledger;

import java.math.BigDecimal;

public record Balance(String bank, String borrower, BigDecimal amountPaid, int emisLeft) {
    @Override public String toString() { return bank + " " + borrower + " " + amountPaid.toPlainString() + " " + emisLeft; }
}
