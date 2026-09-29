package ch08.jpa;

import java.math.BigDecimal;

/** Interface-based (closed) projection: Spring Data selects only these columns. */
public interface BookSummary {
    String getTitle();
    BigDecimal getPrice();
}
