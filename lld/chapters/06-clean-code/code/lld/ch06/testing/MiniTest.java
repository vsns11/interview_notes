package lld.ch06.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * A ~60-line, JUnit-free test runner.
 * - finds methods annotated with @Test (reflection)
 * - creates a FRESH instance of the test class for every test (tests stay Independent)
 * - reports PASS / FAIL / ERROR by itself (Self-validating)
 */
public final class MiniTest {
    private MiniTest() {}

    public record Summary(int passed, int failed) {
        public Summary plus(Summary o) { return new Summary(passed + o.passed, failed + o.failed); }
    }

    public static Summary run(Class<?> testClass) {
        System.out.println(testClass.getSimpleName());
        int passed = 0, failed = 0;
        Method[] tests = Arrays.stream(testClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Test.class))
                .sorted(Comparator.comparing(Method::getName))      // stable output; tests must not depend on order
                .toArray(Method[]::new);
        for (Method m : tests) {
            String name = m.getName().replace('_', ' ');
            try {
                Object instance = testClass.getDeclaredConstructor().newInstance();
                m.setAccessible(true);
                m.invoke(instance);
                passed++;
                System.out.println("  PASS  " + name);
            } catch (InvocationTargetException e) {
                failed++;
                Throwable cause = e.getCause();
                String kind = cause instanceof AssertionError ? "FAIL " : "ERROR";
                System.out.println("  " + kind + " " + name + " -> " + cause);
            } catch (ReflectiveOperationException e) {
                failed++;
                System.out.println("  ERROR " + name + " -> cannot run: " + e);
            }
        }
        return new Summary(passed, failed);
    }

    // ---------- assertions ----------
    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual))
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
    }
    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static <T extends Throwable> T assertThrows(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) return type.cast(t);
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
