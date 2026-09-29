package ch03.switches;

import java.time.DayOfWeek;

/** switch expressions (Java 14): arrow labels, multiple constants, yield, exhaustiveness. */
public class SwitchExpression {
    enum Size { S, M, L, XL }

    static int priceCents(Size size) {
        return switch (size) {                       // no default needed: all enum constants covered
            case S -> 250;
            case M -> 300;
            case L, XL -> 350;
        };
    }

    static String describe(int code) {
        return switch (code) {
            case 200, 201, 204 -> "success";
            case 301, 302 -> "redirect";
            case 404 -> "not found";
            default -> {
                String family = code >= 500 ? "server error" : "other";
                yield family + " (" + code + ")";    // yield returns a value from a block
            }
        };
    }

    public static void main(String[] args) {
        for (Size s : Size.values()) System.out.print(s + "=" + priceCents(s) + " ");
        System.out.println();
        for (int code : new int[] {200, 302, 404, 503, 418}) System.out.println(code + " -> " + describe(code));

        DayOfWeek day = DayOfWeek.WEDNESDAY;
        int letters = switch (day) {
            case MONDAY, FRIDAY, SUNDAY -> 6;
            case TUESDAY -> 7;
            case THURSDAY, SATURDAY -> 8;
            case WEDNESDAY -> 9;
        };
        System.out.println(day + " has " + letters + " letters");

        // yield also works with old-style colon labels inside a switch expression
        String kind = switch (letters % 2) {
            case 0:
                yield "even";
            default:
                yield "odd";
        };
        System.out.println(letters + " is " + kind);
    }
}
