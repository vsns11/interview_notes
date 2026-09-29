package lld.ch21.calendar;

import java.time.LocalDate;

public enum Frequency {
    DAILY { LocalDate next(LocalDate d) { return d.plusDays(1); } },
    WEEKLY { LocalDate next(LocalDate d) { return d.plusWeeks(1); } };

    abstract LocalDate next(LocalDate d);
}
