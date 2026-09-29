package lld.ch21.wallet;

import java.math.BigDecimal;

/** What the caller gets back. A replayed (duplicate) request returns the stored result. */
public record TransferResult(String reference, String from, String to, BigDecimal amount, BigDecimal cashback) { }
