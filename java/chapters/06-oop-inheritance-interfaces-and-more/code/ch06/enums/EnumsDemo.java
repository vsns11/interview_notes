package ch06.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class EnumsDemo {

    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN;         // simple enum: a fixed set of constants
        boolean isWeekend() { return this == SAT || this == SUN; }
    }

    enum Planet {                                          // enum with fields, constructor and methods
        MERCURY(3.303e+23, 2.4397e6),
        EARTH(5.976e+24, 6.37814e6),
        JUPITER(1.9e+27, 7.1492e7);

        private static final double G = 6.67300E-11;
        private final double mass, radius;                 // make enum fields final
        Planet(double mass, double radius) {               // constructor is implicitly private
            this.mass = mass;
            this.radius = radius;
        }
        double surfaceGravity() { return G * mass / (radius * radius); }
    }

    enum Operation {                                       // constant-specific behaviour
        PLUS("+")  { int apply(int a, int b) { return a + b; } },
        TIMES("*") { int apply(int a, int b) { return a * b; } };

        final String symbol;
        Operation(String symbol) { this.symbol = symbol; }
        abstract int apply(int a, int b);                  // each constant must implement it
    }

    static String plan(Day d) {
        return switch (d) {                                // exhaustive over all constants: no default needed
            case SAT, SUN -> "rest";
            case FRI -> "wrap up";
            case MON, TUE, WED, THU -> "work";
        };
    }

    public static void main(String[] args) {
        for (Day d : Day.values())
            System.out.print(d + "(" + d.ordinal() + ")" + (d.isWeekend() ? "* " : " "));
        System.out.println();
        Day d = Day.valueOf("FRI");
        System.out.println(d.name() + " -> " + plan(d) + ", compareTo(MON) = " + d.compareTo(Day.MON)
                + ", == works: " + (d == Day.FRI));
        try {
            Day.valueOf("Friday");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"Friday\") -> IllegalArgumentException");
        }
        for (Planet p : Planet.values())
            System.out.printf("%-8s g = %5.2f m/s^2%n", p, p.surfaceGravity());
        for (Operation op : Operation.values())
            System.out.println("6 " + op.symbol + " 7 = " + op.apply(6, 7));

        EnumSet<Day> weekend = EnumSet.of(Day.SAT, Day.SUN);
        EnumSet<Day> weekdays = EnumSet.complementOf(weekend);
        Map<Day, Integer> hours = new EnumMap<>(Day.class);
        for (Day w : weekdays) hours.put(w, 8);
        hours.put(Day.FRI, 6);
        System.out.println("weekdays=" + weekdays + " hours=" + hours);
    }
}
