package lld.ch21.ledger;

import java.math.BigDecimal;

/** Extra payment made right after EMI number `afterEmi`. */
public record LumpSum(BigDecimal amount, int afterEmi) { }
