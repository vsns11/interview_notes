package lld.ch21.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 10% cashback on a user's first transfer, capped. */
public class FirstTransferOffer implements Offer {
    private final BigDecimal percent;
    private final BigDecimal cap;

    public FirstTransferOffer(String percent, String cap) {
        this.percent = new BigDecimal(percent);
        this.cap = Money.of(cap);
    }

    @Override public String name() { return "FIRST_TRANSFER"; }

    @Override public BigDecimal cashbackFor(Wallet sender, BigDecimal amount, int count) {
        if (count != 1) return BigDecimal.ZERO;
        BigDecimal raw = amount.multiply(percent).divide(BigDecimal.valueOf(100), Money.SCALE, RoundingMode.HALF_EVEN);
        return raw.min(cap);
    }
}
