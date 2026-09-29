package ch12.catalog;

import java.math.BigDecimal;

/** JSON returned by the exchange-rate API: {"from":"EUR","to":"USD","rate":1.08} */
public record ExchangeRate(String from, String to, BigDecimal rate) { }
