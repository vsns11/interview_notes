package lld.ch09.proxy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/** Real subject: pretend each call is a slow lookup. */
class LiveRateService implements RateService {
    private static final Map<String, BigDecimal> PER_USD = Map.of(
            "USD", new BigDecimal("1"), "EUR", new BigDecimal("0.92"), "INR", new BigDecimal("83"));
    int lookups = 0;

    @Override
    public BigDecimal rate(String from, String to) {
        lookups++;
        return PER_USD.get(to).divide(PER_USD.get(from), MathContext.DECIMAL64);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        return RateService.toMoney(amount.multiply(rate(from, to)));   // self-call: goes to THIS object, not through any proxy
    }
}
