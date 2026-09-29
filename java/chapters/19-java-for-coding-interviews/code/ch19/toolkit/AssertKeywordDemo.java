package ch19.toolkit;

import static ch19.Check.*;

/** Why this chapter uses Check.eq instead of the assert keyword. */
public class AssertKeywordDemo {
    public static void main(String[] args) {
        boolean enabled = false;
        assert enabled = true;                  // the side effect runs ONLY when assertions are on (java -ea)
        System.out.println("assert keyword enabled? " + enabled);
        assert 1 + 1 == 3 : "never reported without -ea";
        System.out.println("assert 1 + 1 == 3 did not stop the program");
        eq(1 + 1, 2, "Check.eq always runs");
        try {
            eq(1 + 1, 3, "deliberately wrong");
        } catch (AssertionError e) {
            System.out.println("Check.eq failed loudly: " + e.getMessage());
        }
    }
}
