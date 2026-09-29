package lld.ch21.wallet;

import java.math.BigDecimal;
import java.time.Instant;

/** Immutable ledger entry. Each transfer writes two entries (out + in) sharing one reference. */
public record Transaction(String id, String reference, String walletId, TxnType type, BigDecimal amount,
                          BigDecimal balanceAfter, String counterparty, Instant at) {
    @Override public String toString() {
        return "%-5s %-12s %8s  bal=%8s  ref=%s%s".formatted(id, type, amount, balanceAfter, reference,
                counterparty == null ? "" : " with " + counterparty);
    }
}
