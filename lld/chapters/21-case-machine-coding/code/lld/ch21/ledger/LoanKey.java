package lld.ch21.ledger;

/** A borrower can hold one loan per bank; (bank, borrower) identifies it. */
public record LoanKey(String bank, String borrower) { }
