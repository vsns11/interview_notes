package lld.ch04.srp.after;

import java.math.BigDecimal;

/** Just the data. No group owns a business rule in here. */
public record Employee(String name, int hoursWorked, BigDecimal hourlyRate) { }
