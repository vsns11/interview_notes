package ch03.decisions;

/** if, else if, else; nesting; the dangling-else trap. */
public class IfElse {
    static String grade(int score) {
        if (score < 0 || score > 100) {
            return "invalid";
        } else if (score >= 90) {
            return "A";
        } else if (score >= 80) {
            return "B";
        } else if (score >= 70) {
            return "C";
        } else {
            return "F";
        }
    }

    public static void main(String[] args) {
        for (int s : new int[] {95, 85, 72, 40, 101}) {
            System.out.println(s + " -> " + grade(s));
        }

        int temperature = 35;
        boolean raining = false;
        if (temperature > 30 && !raining) System.out.println("Beach day");   // single statement: braces optional

        // Dangling else: the else belongs to the NEAREST if, whatever the indentation says.
        int x = 5, y = -1;
        if (x > 0)
            if (y > 0)
                System.out.println("both positive");
        else
            System.out.println("printed! this else pairs with 'if (y > 0)'");

        // Conditions must be boolean: if (x) does not compile in Java.
        boolean isAdult = x >= 18;
        if (isAdult) System.out.println("adult"); else System.out.println("minor");
    }
}
