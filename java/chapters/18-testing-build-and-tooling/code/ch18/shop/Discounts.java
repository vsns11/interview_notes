package ch18.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Discount rules, written as pure functions so they are easy to test. */
public final class Discounts {
    private Discounts() {}

    static final BigDecimal BIG_ORDER = new BigDecimal("100.00");

    /** A code looks like 4+ capital letters followed by 2 digits, e.g. SAVE10. */
    public static boolean isValidCode(String code) {
        return code != null && code.matches("[A-Z]{4,}\\d{2}");
    }

    /** SAVE10 gives 10%; SAVE25 gives 25% but only when the subtotal is at least 100; anything else 0%. */
    public static int percentFor(String code, BigDecimal subtotal) {
        if (!isValidCode(code)) return 0;
        return switch (code) {
            case "SAVE10" -> 10;
            case "SAVE25" -> subtotal.compareTo(BIG_ORDER) >= 0 ? 25 : 0;
            default -> 0;
        };
    }

    /** amount minus percent%, rounded half-up to cents. */
    public static BigDecimal apply(BigDecimal amount, int percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("percent out of range: " + percent);
        return amount.multiply(BigDecimal.valueOf(100 - percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
