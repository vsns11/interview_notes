package ch09.jdbc;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;

/** Natural (assigned) id and no @Version: Spring Data cannot tell new from existing on its own. */
public record Product(@Id String sku, String name, BigDecimal price) {}
