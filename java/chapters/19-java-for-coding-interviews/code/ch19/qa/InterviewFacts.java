package ch19.qa;

import java.util.*;

import static ch19.Check.*;

/** Small proofs of classic Java interview answers. Each check is a claim from the Q&A section. */
public class InterviewFacts {
    /** equals overridden but hashCode NOT: breaks every hash-based collection. */
    static final class BadPoint {
        final int x, y;
        BadPoint(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            return o instanceof BadPoint p && p.x == x && p.y == y;
        }
    }

    record Point(int x, int y) {}                 // record: equals, hashCode, toString generated

    /** A return in finally silently replaces the try's return (and would swallow exceptions). */
    @SuppressWarnings("finally")
    static int finallyWins() {
        try { return 1; } finally { return 2; }
    }

    static final class Res implements AutoCloseable {
        private final String name; private final List<String> log;
        Res(String name, List<String> log) { this.name = name; this.log = log; log.add("open " + name); }
        @Override public void close() { log.add("close " + name); }
    }

    public static void main(String[] args) {
        // Q: String pool and ==
        String a = "hi", b = "hi", c = new String("hi");
        isTrue(a == b, "literals share one pooled object");
        isTrue(a != c, "new String makes a separate object");
        isTrue(a == c.intern(), "intern() returns the pooled copy");
        String h = "h";
        isTrue(a != h + "i", "runtime concatenation creates a new object");
        isTrue(a == "h" + "i", "compile-time constant concatenation is pooled");

        // Q: equals without hashCode
        Set<BadPoint> bad = new HashSet<>(List.of(new BadPoint(1, 2)));
        isTrue(new BadPoint(1, 2).equals(new BadPoint(1, 2)), "BadPoint equals says equal");
        isTrue(!bad.contains(new BadPoint(1, 2)), "but HashSet cannot find it (different hashCode)");
        isTrue(new HashSet<>(List.of(new Point(1, 2))).contains(new Point(1, 2)), "record works as a key");

        // Q: final / finally
        eq(finallyWins(), 2, "return in finally overrides try");

        // Q: try-with-resources closes in reverse order
        List<String> log = new ArrayList<>();
        try (Res r1 = new Res("A", log); Res r2 = new Res("B", log)) {
            log.add("body");
        }
        eq(log, List.of("open A", "open B", "body", "close B", "close A"), "try-with-resources order");

        // Q: generics are erased at run time
        isTrue(new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass(), "same runtime class");

        // Q: fail-fast iterator
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));
        boolean threw = false;
        try { for (Integer n : nums) nums.add(n); } catch (ConcurrentModificationException e) { threw = true; }
        isTrue(threw, "adding during for-each throws ConcurrentModificationException");

        // Q: Java is pass-by-value (the reference is copied)
        StringBuilder sb = new StringBuilder("x");
        reassign(sb);
        eq(sb.toString(), "x!", "callee can mutate the object but not rebind the caller's variable");

        // Q: immutable vs unmodifiable view
        List<Integer> backing = new ArrayList<>(List.of(1));
        List<Integer> view = Collections.unmodifiableList(backing);
        List<Integer> copy = List.copyOf(backing);
        backing.add(2);
        eq(view.size() + "," + copy.size(), "2,1", "view sees later changes, copy does not");

        // Q: Optional misuse
        Optional<String> empty = Optional.empty();
        eq(empty.orElse("default"), "default", "orElse instead of get()");
        boolean nse = false;
        try { empty.get(); } catch (NoSuchElementException e) { nse = true; }
        isTrue(nse, "get() on empty throws NoSuchElementException");
        done();
    }

    static void reassign(StringBuilder s) {
        s.append('!');                // visible to the caller: same object
        s = new StringBuilder("y");   // invisible to the caller: only the local copy changes
    }
}
