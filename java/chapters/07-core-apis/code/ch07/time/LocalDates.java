package ch07.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class LocalDates {
    public static void main(String[] args) {
        // In real code: LocalDate.now(). Fixed values keep this output reproducible.
        LocalDate release = LocalDate.of(2025, Month.SEPTEMBER, 16);     // Java 25 GA date
        LocalTime standup = LocalTime.of(9, 30);
        LocalDateTime meeting = LocalDateTime.of(release, standup);
        System.out.println("date=" + release + " time=" + standup + " dateTime=" + meeting);
        System.out.println("dayOfWeek=" + release.getDayOfWeek() + " dayOfYear=" + release.getDayOfYear()
                + " month=" + release.getMonth() + " monthValue=" + release.getMonthValue()
                + " leapYear=" + release.isLeapYear() + " lengthOfMonth=" + release.lengthOfMonth());

        // Immutable: plus/minus/with return NEW objects
        LocalDate d = LocalDate.of(2024, 1, 31);
        d.plusDays(1);                                               // result ignored -> d unchanged
        System.out.println("d after ignored plusDays: " + d);
        System.out.println("2024-01-31 plusMonths(1)=" + d.plusMonths(1) + " (clamped to month end, leap year)");
        System.out.println("2025-01-31 plusMonths(1)=" + LocalDate.of(2025, 1, 31).plusMonths(1));
        System.out.println("withDayOfMonth(1)=" + release.withDayOfMonth(1)
                + " lastDayOfMonth=" + release.with(TemporalAdjusters.lastDayOfMonth())
                + " nextFriday=" + release.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)));

        // Comparing
        LocalDate lts21 = LocalDate.of(2023, 9, 19);
        System.out.println("isBefore=" + lts21.isBefore(release) + " isAfter=" + lts21.isAfter(release)
                + " equals=" + lts21.equals(LocalDate.parse("2023-09-19")));

        // Period (date-based) vs ChronoUnit.between (a single unit)
        Period gap = Period.between(lts21, release);
        System.out.println("Period between Java 21 and 25 GA: " + gap + " = " + gap.getYears() + "y "
                + gap.getMonths() + "m " + gap.getDays() + "d; total days=" + ChronoUnit.DAYS.between(lts21, release)
                + " total months=" + ChronoUnit.MONTHS.between(lts21, release));

        // Age calculation
        LocalDate birthday = LocalDate.of(1990, 2, 28), today = LocalDate.of(2026, 2, 27);
        System.out.println("age on " + today + " = " + Period.between(birthday, today).getYears());

        // Iterating a range: Java 9 datesUntil (end exclusive)
        System.out.print("weekdays in week of 2026-03-02: ");
        LocalDate.of(2026, 3, 2).datesUntil(LocalDate.of(2026, 3, 9))
                .filter(x -> x.getDayOfWeek().getValue() <= 5)
                .forEach(x -> System.out.print(x.getDayOfMonth() + " "));
        System.out.println();

        YearMonth feb = YearMonth.of(2028, 2);
        System.out.println("YearMonth " + feb + " has " + feb.lengthOfMonth() + " days");

        try {
            LocalDate.of(2025, 2, 29);
        } catch (java.time.DateTimeException e) {
            System.out.println("DateTimeException: " + e.getMessage());
        }
    }
}
