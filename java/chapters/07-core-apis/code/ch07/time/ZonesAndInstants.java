package ch07.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ZonesAndInstants {
    public static void main(String[] args) {
        // Instant: a point on the UTC timeline (seconds + nanos since 1970-01-01T00:00Z)
        Instant epoch = Instant.EPOCH;
        Instant t = Instant.ofEpochSecond(1_758_000_000L);
        System.out.println("epoch=" + epoch + " t=" + t + " t.toEpochMilli()=" + t.toEpochMilli());

        // The same instant seen in three zones
        ZoneId ny = ZoneId.of("America/New_York"), kolkata = ZoneId.of("Asia/Kolkata"), tokyo = ZoneId.of("Asia/Tokyo");
        System.out.println("NY:      " + t.atZone(ny));
        System.out.println("Kolkata: " + t.atZone(kolkata));
        System.out.println("Tokyo:   " + t.atZone(tokyo));

        // Converting a meeting time between zones keeps the instant
        ZonedDateTime call = ZonedDateTime.of(LocalDateTime.of(2026, 3, 20, 9, 0), ny);
        System.out.println("9:00 NY call in Kolkata: " + call.withZoneSameInstant(kolkata).toLocalTime()
                + "; withZoneSameLocal keeps 09:00 but changes the instant: " + call.withZoneSameLocal(kolkata));

        // DST gap: 2026-03-08 02:30 does not exist in New York (clocks jump 02:00 -> 03:00)
        ZonedDateTime gap = ZonedDateTime.of(LocalDateTime.of(2026, 3, 8, 2, 30), ny);
        System.out.println("02:30 in the gap becomes: " + gap);

        // Duration (exact time) vs Period (calendar) across the DST change
        ZonedDateTime before = ZonedDateTime.of(LocalDateTime.of(2026, 3, 7, 12, 0), ny);
        System.out.println("plus(Period.ofDays(1)) = " + before.plus(Period.ofDays(1)));
        System.out.println("plus(Duration.ofDays(1)) = " + before.plus(Duration.ofDays(1)));
        System.out.println("hours between noon and noon next day: "
                + Duration.between(before, before.plusDays(1)).toHours());

        Duration d = Duration.ofMinutes(135).plusSeconds(30);
        System.out.println("Duration " + d + " = " + d.toHoursPart() + "h " + d.toMinutesPart() + "m " + d.toSecondsPart()
                + "s; Period.of(1,2,3)=" + Period.of(1, 2, 3) + " Period.ofWeeks(2)=" + Period.ofWeeks(2));

        // OffsetDateTime: fixed offset, no zone rules (common in APIs/databases)
        OffsetDateTime odt = OffsetDateTime.of(2026, 3, 20, 9, 0, 0, 0, ZoneOffset.ofHours(-4));
        System.out.println("OffsetDateTime=" + odt + " toInstant=" + odt.toInstant());

        // Clock: inject time for testable code
        Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        System.out.println("LocalDate.now(fixedClock)=" + LocalDate.now(fixed) + " Instant.now(fixedClock)=" + Instant.now(fixed));

        // Legacy interop
        java.util.Date legacy = java.util.Date.from(t);
        System.out.println("Date.from(t).getTime()==t.toEpochMilli(): " + (legacy.getTime() == t.toEpochMilli())
                + ", back to Instant: " + legacy.toInstant());
    }
}
