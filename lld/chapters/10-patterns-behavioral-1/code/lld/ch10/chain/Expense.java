package lld.ch10.chain;

import java.math.BigDecimal;

/** An expense claim that needs someone with enough authority to approve it. Money is BigDecimal, never double. */
record Expense(String id, BigDecimal amountUsd) {}
