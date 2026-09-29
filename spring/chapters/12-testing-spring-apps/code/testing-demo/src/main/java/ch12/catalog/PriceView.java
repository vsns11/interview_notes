package ch12.catalog;

import java.math.BigDecimal;

public record PriceView(String sku, String name, BigDecimal amount, String currency) { }
