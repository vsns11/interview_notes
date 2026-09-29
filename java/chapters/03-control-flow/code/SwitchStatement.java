package ch03.switches;

/** The classic switch statement: colon labels, break, fall-through, default. */
public class SwitchStatement {
    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        int month = 4;
        int days;
        switch (month) {
            case 2:
                days = 28;
                break;
            case 4: case 6: case 9: case 11:        // several labels share one body
                days = 30;
                break;
            default:
                days = 31;
        }
        System.out.println("month " + month + " has " + days + " days");

        String command = "stop";
        switch (command) {                            // String switch: Java 7, uses equals()
            case "start" -> System.out.println("starting");
            case "stop" -> System.out.println("stopping");
            default -> System.out.println("unknown");
        }

        System.out.println("Fall-through without break (level 2):");
        int level = 2;
        switch (level) {
            case 1:
                System.out.println("  unlock level 1 perks");
            case 2:
                System.out.println("  unlock level 2 perks");
            case 3:
                System.out.println("  unlock level 3 perks");   // runs too: no break above
                break;
            case 4:
                System.out.println("  not reached");
        }

        Day today = Day.SAT;
        switch (today) {                              // enum: bare constant names in labels
            case SAT, SUN -> System.out.println(today + " is weekend");
            default -> System.out.println(today + " is a weekday");
        }
        char c = 'b';
        switch (c) {
            case 'a', 'e', 'i', 'o', 'u' -> System.out.println(c + " is a vowel");
            default -> System.out.println(c + " is a consonant");
        }
    }
}
