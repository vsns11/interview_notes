package ch08.jpa;

import java.math.BigDecimal;

/** Class-based (DTO) projection as a record. */
public record BookRow(String title, BigDecimal price) {}
