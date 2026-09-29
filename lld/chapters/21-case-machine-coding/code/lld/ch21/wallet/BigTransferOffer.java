package lld.ch21.wallet;

import java.math.BigDecimal;

/** Flat cashback for any transfer at or above a threshold. */
public class BigTransferOffer implements Offer {
    private final BigDecimal threshold;
    private final BigDecimal flat;

    public BigTransferOffer(String threshold, String flat) {
        this.threshold = Money.of(threshold);
        this.flat = Money.of(flat);
    }

    @Override public String name() { return "BIG_TRANSFER"; }

    @Override public BigDecimal cashbackFor(Wallet sender, BigDecimal amount, int count) {
        return amount.compareTo(threshold) >= 0 ? flat : BigDecimal.ZERO;
    }
}
