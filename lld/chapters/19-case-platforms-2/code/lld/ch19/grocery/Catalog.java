package lld.ch19.grocery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Money helpers: BigDecimal, 2 decimal places, HALF_UP (the usual retail rounding). Never double. */
final class Money {
    private Money() { }
    static final BigDecimal ZERO = of("0");
    static BigDecimal of(String s) { return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP); }
    static BigDecimal round(BigDecimal b) { return b.setScale(2, RoundingMode.HALF_UP); }
    static String fmt(BigDecimal b) { return "Rs " + round(b).toPlainString(); }
}

enum Department { PRODUCE, DAIRY, SNACKS, HOUSEHOLD }

/** How the shelf price is quoted: per piece, or per kilogram (weighed at the till). */
enum PricingUnit { EACH, PER_KG }

/** Tax slab. Rates are illustrative (loosely modelled on India's GST slabs), not tax advice. */
enum TaxCategory {
    EXEMPT("0"), REDUCED("5"), STANDARD("18");
    final BigDecimal ratePercent;
    TaxCategory(String r) { ratePercent = new BigDecimal(r); }
}

/** code = barcode (EAN-13) for packaged goods, or a 4-digit PLU for loose produce. */
record Product(String sku, String code, String name, Department dept, PricingUnit unit,
               BigDecimal unitPrice, TaxCategory tax) {
    Product {
        if (unitPrice.signum() <= 0) throw new PosException("Price must be positive: " + name);
        unitPrice = Money.round(unitPrice);
    }
}

/** Look-up by scanned code. Read far more than written, so a ConcurrentHashMap shared by all registers. */
final class Catalog {
    private final Map<String, Product> byCode = new ConcurrentHashMap<>();

    void add(Product p) {
        if (byCode.putIfAbsent(p.code(), p) != null) throw new PosException("Duplicate code " + p.code());
    }

    /** Price changes replace the record; open sales keep the price they already scanned. */
    void reprice(String code, BigDecimal newPrice) {
        byCode.computeIfPresent(code, (c, p) -> new Product(p.sku(), c, p.name(), p.dept(), p.unit(), newPrice, p.tax()));
    }

    Optional<Product> find(String code) { return Optional.ofNullable(byCode.get(code)); }
}

final class PosException extends RuntimeException {
    PosException(String message) { super(message); }
}
