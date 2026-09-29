package ch18.shop;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(String id, String customer, BigDecimal total, Instant placedAt) {}
