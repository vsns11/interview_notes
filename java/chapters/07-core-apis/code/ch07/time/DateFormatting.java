package ch07.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateFormatting {
    public static void main(String[] args) {
        LocalDateTime dt = LocalDateTime.of(2025, 12, 29, 14, 5, 9);
        System.out.println("toString (ISO-8601): " + dt);
        System.out.println("ISO_LOCAL_DATE: " + dt.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("BASIC_ISO_DATE: " + dt.format(DateTimeFormatter.BASIC_ISO_DATE));

        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM uuuu, hh:mm a", Locale.US);
        System.out.println("custom: " + dt.format(f));
        System.out.println("EEEE d MMMM (fr): " + dt.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRANCE)));
        System.out.println("MEDIUM (US): " + dt.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US)));

        // The week-based-year trap: YYYY is NOT the calendar year
        System.out.println("yyyy-MM-dd: " + dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "   YYYY-MM-dd: " + dt.format(DateTimeFormatter.ofPattern("YYYY-MM-dd", Locale.US)));
        // mm is minutes, MM is month
        System.out.println("MM=" + dt.format(DateTimeFormatter.ofPattern("MM")) + " mm=" + dt.format(DateTimeFormatter.ofPattern("mm")));

        // Parsing
        LocalDate parsed = LocalDate.parse("03/08/2026", DateTimeFormatter.ofPattern("MM/dd/uuuu"));
        System.out.println("parsed=" + parsed + " " + parsed.getDayOfWeek());
        try {
            LocalDate.parse("2026-13-01");
        } catch (DateTimeParseException e) {
            System.out.println("DateTimeParseException: " + e.getMessage());
        }

        ZonedDateTime z = ZonedDateTime.of(dt, ZoneId.of("Europe/London"));
        System.out.println("ISO_OFFSET_DATE_TIME: " + z.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                + " | zone pattern: " + z.format(DateTimeFormatter.ofPattern("HH:mm VV (zzz)", Locale.US)));
    }
}
