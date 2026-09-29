package ch19;

import java.util.Arrays;
import java.util.Objects;

/**
 * A tiny assertion helper for interview-style practice programs.
 * Java's own {@code assert} keyword is DISABLED unless you run with {@code java -ea},
 * so a silent pass would prove nothing. These checks always run.
 */
public final class Check {
    private static int passed = 0;

    private Check() {}

    /** Fails loudly if actual is not equal to expected (arrays are compared by content). */
    public static void eq(Object actual, Object expected, String label) {
        boolean same = (actual instanceof int[] a && expected instanceof int[] e) ? Arrays.equals(a, e)
                : (actual instanceof Object[] a2 && expected instanceof Object[] e2) ? Arrays.deepEquals(a2, e2)
                : Objects.equals(actual, expected);
        if (!same) {
            throw new AssertionError(label + ": expected " + show(expected) + " but got " + show(actual));
        }
        passed++;
        System.out.println("ok  " + label + " -> " + show(actual));
    }

    /** Fails loudly if the condition is false. */
    public static void isTrue(boolean condition, String label) {
        eq(condition, true, label);
    }

    /** Prints how many checks passed; call at the end of main. */
    public static void done() {
        System.out.println(passed + " checks passed");
    }

    private static String show(Object o) {
        if (o instanceof int[] a) return Arrays.toString(a);
        if (o instanceof Object[] a) return Arrays.deepToString(a);
        return String.valueOf(o);
    }
}
