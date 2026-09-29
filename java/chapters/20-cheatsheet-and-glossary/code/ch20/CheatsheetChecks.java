package ch20;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Asserts every concrete value quoted on the Chapter 20 cheat sheets.
 * Each sheet is one method; a failed check throws AssertionError (the program exits non-zero).
 * Java's assert keyword is off by default, so this uses its own check helper.
 */
public class CheatsheetChecks {
    private static int total = 0;
    private static int inSheet = 0;

    static void eq(Object actual, Object expected, String what) {
        if (!Objects.equals(actual, expected))
            throw new AssertionError(what + ": expected <" + expected + "> but got <" + actual + ">");
        inSheet++;
    }

    static void yes(boolean condition, String what) { eq(condition, true, what); }

    /** Runs code that must throw the given exception type. */
    static void throwsA(Class<? extends Throwable> type, Runnable code, String what) {
        try {
            code.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { inSheet++; return; }
            throw new AssertionError(what + ": expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    static void sheet(String name, Runnable body) {
        inSheet = 0;
        body.run();
        total += inSheet;
        System.out.printf("OK   %-40s %2d checks%n", name, inSheet);
    }

    // ---------------------------------------------------------------- 20.1
    static void syntaxAndTypes() {
        eq(Byte.MIN_VALUE + "…" + Byte.MAX_VALUE, "-128…127", "byte range");
        eq(Short.MIN_VALUE + "…" + Short.MAX_VALUE, "-32768…32767", "short range");
        eq(Integer.MIN_VALUE + "…" + Integer.MAX_VALUE, "-2147483648…2147483647", "int range");
        eq(Long.MAX_VALUE, 9223372036854775807L, "long max");
        eq((int) Character.MIN_VALUE + "…" + (int) Character.MAX_VALUE, "0…65535", "char range");
        eq(Float.MAX_VALUE, 3.4028235E38f, "float max");
        eq(Double.MAX_VALUE, 1.7976931348623157E308, "double max");
        yes(Double.MIN_VALUE > 0, "Double.MIN_VALUE is the smallest POSITIVE double");
        eq(Integer.SIZE + "/" + Long.SIZE + "/" + Character.SIZE, "32/64/16", "bit sizes");
        eq(0x1F + 0b101 + 017 + 1_000, 31 + 5 + 15 + 1000, "hex, binary, octal, underscore literals");
        eq(Integer.MAX_VALUE + 1, Integer.MIN_VALUE, "int overflow wraps silently");
        throwsA(ArithmeticException.class, () -> Math.addExact(Integer.MAX_VALUE, 1), "addExact detects overflow");
        eq(7 / 2, 3, "int division truncates");
        eq(-7 / 2, -3, "int division truncates toward zero");
        eq(-7 % 3, -1, "% takes the sign of the dividend");
        eq(Math.floorMod(-7, 3), 2, "floorMod is never negative for a positive divisor");
        eq(7 / 2.0, 3.5, "one double operand -> double division");
        eq(0.1 + 0.2, 0.30000000000000004, "binary floating point is inexact");
        eq(1.0 / 0, Double.POSITIVE_INFINITY, "double / 0 -> Infinity");
        throwsA(ArithmeticException.class, () -> { int z = 0; System.out.print(1 / z); }, "int / 0 throws");
        yes(Double.isNaN(0.0 / 0), "0.0/0 is NaN");
        yes(Double.NaN != Double.NaN, "NaN is not equal to itself");
        eq((int) 3.99, 3, "cast to int truncates");
        eq((byte) 200, (byte) -56, "narrowing keeps the low 8 bits");
        eq((int) 'A', 65, "char is a number");
        eq((char) ('a' + 1), 'b', "char arithmetic");
        byte b = 10;
        b += 300;                                  // compound assignment hides a cast
        eq(b, (byte) 54, "b += 300 is b = (byte)(b + 300)");
        var list = new ArrayList<String>();        // var infers ArrayList<String>
        eq(list.getClass().getSimpleName(), "ArrayList", "var infers the initializer's type");
        String block = """
                Hi
                  there
                """;
        eq(block, "Hi\n  there\n", "text block strips common indentation, ends with \\n");
    }

    // ---------------------------------------------------------------- 20.2
    static void operators() {
        eq(2 + 3 * 4, 14, "* before +");
        eq(10 - 4 - 3, 3, "left associative");
        eq("" + 1 + 2 + "3" + 4 + 5, "12345", "string + from the left");
        eq(1 + 2 + "3" + 4 + 5, "3345", "numbers added before the first String");
        eq(true || false && false, true, "&& binds tighter than ||");
        int x = 6;
        eq((x & 1) == 0, true, "parenthesise bitwise tests");
        int i = 5;
        int j = i++ + ++i;                          // 5 + 7
        eq(j, 12, "i++ + ++i");
        eq(-8 >> 1, -4, ">> keeps the sign");
        eq(-8 >>> 28, 15, ">>> fills with zeros");
        eq(1 << 10, 1024, "shift left");
        eq(5 ^ 3, 6, "xor");
        eq(~5, -6, "bitwise complement is -(n+1)");
        int a, bb, c;
        a = bb = c = 7;
        eq(a + bb + c, 21, "assignment is right associative");
        String s = null;
        eq(s != null && s.length() > 0, false, "&& short-circuits (no NPE)");
        eq(x > 3 ? "big" : "small", "big", "ternary");
        Object o = "text";
        eq(o instanceof String str && str.length() == 4, true, "instanceof pattern binds a variable");
    }

    // ---------------------------------------------------------------- 20.3
    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double side) implements Shape {}
    record Point(int x, int y) {}

    static String kind(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer n when n < 0 -> "negative";
            case Integer n -> "int";
            case String str -> "string";
            case Point(int px, int py) when px == py -> "diagonal point";
            case Point(var px, _) -> "point x=" + px;
            default -> "other";
        };
    }

    static double area(Shape s) {
        return switch (s) {                         // exhaustive: sealed, no default
            case Circle c -> Math.PI * c.r() * c.r();
            case Square q -> q.side() * q.side();
        };
    }

    static void controlFlow() {
        int day = 6;
        String type = switch (day) {
            case 1, 2, 3, 4, 5 -> "weekday";
            case 6, 7 -> "weekend";
            default -> { yield "invalid"; }
        };
        eq(type, "weekend", "switch expression with arrow labels");
        StringBuilder fall = new StringBuilder();
        switch (2) {                                // old-style colon labels fall through
            case 1: fall.append("one ");
            case 2: fall.append("two ");
            case 3: fall.append("three");
        }
        eq(fall.toString(), "two three", "colon-label switch falls through without break");
        eq(kind(null), "null", "case null");
        eq(kind(-3), "negative", "guarded pattern");
        eq(kind(4), "int", "type pattern");
        eq(kind(new Point(2, 2)), "diagonal point", "record pattern + guard");
        eq(kind(new Point(1, 9)), "point x=1", "record pattern with var and _");
        eq(kind(2.5), "other", "default");
        eq(area(new Square(3)), 9.0, "sealed switch needs no default");
        throwsA(NullPointerException.class, () -> { String n = null; switch (n) { case "a" -> {} default -> {} } },
                "switch on null without case null throws NPE");
        int count = 0;
        outer:
        for (int r = 0; r < 3; r++) {
            for (int col = 0; col < 3; col++) {
                if (col == 2) continue outer;
                if (r == 2) break outer;
                count++;
            }
        }
        eq(count, 4, "labelled break/continue");
        int n = 10;
        do { n++; } while (n < 5);
        eq(n, 11, "do-while body runs at least once");
    }

    // ---------------------------------------------------------------- 20.4
    static void change(int v, int[] arr, StringBuilder sb) { v = 99; arr[0] = 99; sb.append("!"); sb = new StringBuilder("new"); }
    static String over(long v) { return "long"; }
    static String over(Integer v) { return "Integer"; }
    static String over(int... v) { return "varargs"; }
    static int sum(int... nums) { return Arrays.stream(nums).sum(); }

    static void methodsAndArrays() {
        int v = 1; int[] arr = {1}; StringBuilder sb = new StringBuilder("x");
        change(v, arr, sb);
        eq(v + "," + arr[0] + "," + sb, "1,99,x!", "pass-by-value (of references too)");
        eq(over(5), "long", "overload: widening beats boxing beats varargs");
        eq(sum(), 0, "varargs accepts zero arguments");
        eq(sum(1, 2, 3), 6, "varargs");
        int[] zeros = new int[3];
        eq(Arrays.toString(zeros), "[0, 0, 0]", "arrays are zero-initialised");
        String[] names = new String[2];
        eq(Arrays.toString(names), "[null, null]", "reference arrays start as null");
        int[][] jagged = {{1}, {2, 3}, {}};
        eq(jagged[1].length + jagged[2].length, 2, "jagged rows have their own lengths");
        eq(Arrays.deepToString(jagged), "[[1], [2, 3], []]", "deepToString");
        int[] a1 = {3, 1, 2}, a2 = {3, 1, 2};
        eq(a1.equals(a2), false, "array equals is identity");
        eq(Arrays.equals(a1, a2), true, "Arrays.equals compares contents");
        Arrays.sort(a1);
        eq(Arrays.toString(a1), "[1, 2, 3]", "Arrays.sort");
        eq(Arrays.binarySearch(a1, 2), 1, "binarySearch on sorted array");
        eq(Arrays.binarySearch(a1, 5), -4, "not found -> -(insertionPoint) - 1");
        int[] copy = Arrays.copyOf(a1, 5);
        eq(Arrays.toString(copy), "[1, 2, 3, 0, 0]", "copyOf pads with defaults");
        throwsA(ArrayIndexOutOfBoundsException.class, () -> { int[] two = new int[2]; System.out.print(two[2]); }, "index == length throws");
        List<Integer> fixed = Arrays.asList(1, 2, 3);
        throwsA(UnsupportedOperationException.class, () -> fixed.add(4), "Arrays.asList is fixed-size");
    }

    // ---------------------------------------------------------------- 20.5
    enum Planet {
        MERCURY(3.303e+23), EARTH(5.976e+24);
        final double mass;
        Planet(double mass) { this.mass = mass; }
    }

    static class Base {
        static String who() { return "Base"; }
        String me() { return "Base"; }
        String call() { return me() + "/" + who(); }
    }

    static class Derived extends Base {
        static String who() { return "Derived"; }       // hides, doesn't override
        @Override String me() { return "Derived"; }
    }

    static class Validated extends Base {
        final int size;
        Validated(int size) {
            if (size < 0) throw new IllegalArgumentException("size");   // Java 25: statements before super()
            this.size = size;
            super();
        }
    }

    record Range(int lo, int hi) {
        Range {                                          // compact constructor validates
            if (lo > hi) throw new IllegalArgumentException(lo + " > " + hi);
        }
    }

    static void oop() {
        Base b = new Derived();
        eq(b.call(), "Derived/Base", "instance methods dispatch dynamically, statics don't");
        eq(new Point(1, 2), new Point(1, 2), "records have value equality");
        eq(new Point(1, 2).toString(), "Point[x=1, y=2]", "record toString");
        eq(new Point(1, 2).hashCode(), new Point(1, 2).hashCode(), "equal records -> equal hashCode");
        throwsA(IllegalArgumentException.class, () -> new Range(5, 1), "compact constructor");
        throwsA(IllegalArgumentException.class, () -> new Validated(-1), "flexible constructor body (25)");
        eq(Planet.EARTH.ordinal(), 1, "ordinal is declaration position");
        eq(Planet.valueOf("EARTH"), Planet.EARTH, "valueOf by name");
        eq(Planet.values().length, 2, "values()");
        throwsA(IllegalArgumentException.class, () -> Planet.valueOf("PLUTO"), "unknown enum name");
        eq(Shape.class.isSealed(), true, "sealed interface");
        eq(Point.class.isRecord() && java.lang.reflect.Modifier.isFinal(Point.class.getModifiers()), true,
                "records are final");
        Object plain = new Object();
        eq(plain.equals(new Object()), false, "Object.equals is identity");
    }

    // ---------------------------------------------------------------- 20.6
    static void coreApis() {
        String lit1 = "java", lit2 = "java";
        String made = new String("java");
        eq(lit1 == lit2, true, "literals share one pooled object");
        eq(lit1 == made, false, "new String is a new object");
        eq(lit1 == made.intern(), true, "intern returns the pooled copy");
        String part = "ja";
        eq(lit1 == part + "va", false, "run-time concatenation creates a new object");
        eq(lit1 == "ja" + "va", true, "constant concatenation is folded and pooled");
        eq("Java".substring(1, 3), "av", "substring end is exclusive");
        eq("a,b,,".split(",").length, 2, "split drops trailing empty strings");
        eq("a,b,,".split(",", -1).length, 4, "limit -1 keeps them");
        eq("  hi ".strip(), "hi", "strip");
        eq(" ".isBlank() && !" ".isEmpty(), true, "isBlank vs isEmpty");
        eq("ab".repeat(3), "ababab", "repeat");
        eq("a.b".replace(".", "-") + " " + "a.b".replaceAll(".", "-"), "a-b ---", "replace literal vs replaceAll regex");
        eq("apple".compareTo("banana") < 0, true, "compareTo sign");
        eq(String.join("|", List.of("a", "b")), "a|b", "String.join");
        eq(new StringBuilder("abc").reverse().insert(0, '>').toString(), ">cba", "StringBuilder is mutable");
        eq(String.format(java.util.Locale.US, "%5.2f|%-4s|%04d|%,d", 3.14159, "ab", 42, 1234567),
                " 3.14|ab  |0042|1,234,567", "format specifiers");
        eq(Math.round(2.5) + " " + Math.round(-2.5), "3 -2", "round: ties toward +infinity");
        eq(Math.rint(2.5), 2.0, "rint: ties to even");
        eq(Math.floorDiv(-7, 2), -4, "floorDiv");
        eq(Math.abs(Integer.MIN_VALUE), Integer.MIN_VALUE, "abs(MIN_VALUE) overflows");
        eq(Math.clamp(15, 0, 10), 10, "clamp (21)");
        Integer i1 = 127, i2 = 127, i3 = 128, i4 = 128;
        eq(i1 == i2, true, "Integer cache -128..127");
        eq(i3 == i4, false, "outside the cache: different objects");
        eq(i3.equals(i4), true, "compare wrappers with equals");
        Integer nothing = null;
        throwsA(NullPointerException.class, () -> { int bad = nothing; }, "unboxing null");
        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
        nums.remove(1);                                  // remove(int index)
        nums.remove(Integer.valueOf(10));                // remove(Object)
        eq(nums, List.of(30), "remove(int) vs remove(Object)");
        eq(Integer.parseInt("ff", 16), 255, "parseInt radix");
        eq(Boolean.parseBoolean("YES"), false, "only \"true\" parses to true");
        eq(new BigDecimal("0.1").add(new BigDecimal("0.2")), new BigDecimal("0.3"), "BigDecimal is exact");
        eq(new BigDecimal(0.1).toString().startsWith("0.1000000000000000055"), true, "new BigDecimal(double) is inexact");
        eq(new BigDecimal("2.0").equals(new BigDecimal("2.00")), false, "BigDecimal.equals checks scale");
        eq(new BigDecimal("2.0").compareTo(new BigDecimal("2.00")), 0, "compareTo ignores scale");
        eq(new BigDecimal("2.345").setScale(2, RoundingMode.HALF_EVEN), new BigDecimal("2.34"), "HALF_EVEN (banker's)");
        eq(new BigDecimal("2.345").setScale(2, RoundingMode.HALF_UP), new BigDecimal("2.35"), "HALF_UP");
        throwsA(ArithmeticException.class, () -> BigDecimal.ONE.divide(BigDecimal.valueOf(3)), "1/3 without a scale throws");
        eq(BigDecimal.ONE.divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP), new BigDecimal("0.3333"), "divide with scale");
        eq(BigInteger.TWO.pow(100).toString().length(), 31, "BigInteger: 2^100 has 31 digits");
        eq(LocalDate.of(2024, 1, 31).plusMonths(1), LocalDate.of(2024, 2, 29), "plusMonths clamps to month end");
        eq(Period.between(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 3, 20)).toString(), "P1Y2M5D", "Period");
        eq(Duration.ofMinutes(90).toString(), "PT1H30M", "Duration");
        ZoneId ny = ZoneId.of("America/New_York");
        ZonedDateTime before = ZonedDateTime.of(LocalDateTime.of(2025, 3, 9, 1, 30), ny);
        eq(before.plusHours(1).toLocalTime().toString(), "03:30", "DST spring-forward skips 02:xx");
        eq(Duration.between(before, before.plusDays(1)).toHours(), 23L, "that calendar day has 23 hours");
        eq(LocalDate.of(2025, 12, 29).format(DateTimeFormatter.ofPattern("yyyy YYYY", java.util.Locale.US)), "2025 2026",
                "YYYY is week-based year: a classic bug");
        throwsA(java.time.DateTimeException.class, () -> LocalDate.of(2025, 2, 29), "invalid date");
        eq(new java.util.Random(42).nextInt(100), new java.util.Random(42).nextInt(100), "same seed, same sequence");
    }

    // ---------------------------------------------------------------- 20.7
    static class Res implements AutoCloseable {
        final String name; final List<String> log;
        Res(String name, List<String> log) { this.name = name; this.log = log; log.add("open " + name); }
        @Override public void close() { log.add("close " + name); throw new IllegalStateException("close " + name); }
    }

    @SuppressWarnings("finally")
    static int finallyWins() { try { return 1; } finally { return 2; } }

    static void exceptions() {
        List<String> log = new ArrayList<>();
        try (Res a = new Res("A", log); Res b = new Res("B", log)) {
            log.add("body");
            throw new RuntimeException("body failed");
        } catch (RuntimeException e) {
            eq(e.getMessage(), "body failed", "body exception wins");
            eq(e.getSuppressed().length, 2, "close exceptions are suppressed, not lost");
        }
        eq(log, List.of("open A", "open B", "body", "close B", "close A"), "resources close in reverse order");
        eq(finallyWins(), 2, "return in finally overrides try");
        eq(RuntimeException.class.getSuperclass(), Exception.class, "RuntimeException extends Exception");
        eq(Exception.class.getSuperclass(), Throwable.class, "Exception extends Throwable");
        eq(Error.class.getSuperclass(), Throwable.class, "Error extends Throwable");
        yes(!RuntimeException.class.isAssignableFrom(IOException.class), "IOException is checked");
        yes(RuntimeException.class.isAssignableFrom(IllegalArgumentException.class), "IAE is unchecked");
        yes(RuntimeException.class.isAssignableFrom(java.io.UncheckedIOException.class), "UncheckedIOException is unchecked");
        Exception root = new IOException("disk");
        RuntimeException wrapped = new IllegalStateException("load failed", root);
        eq(wrapped.getCause(), root, "chaining keeps the cause");
        String msg = null;
        try { Object o = null; o.hashCode(); } catch (NullPointerException npe) { msg = npe.getMessage(); }
        eq(msg.startsWith("Cannot invoke \"Object.hashCode()\""), true, "helpful NullPointerException message (14)");
    }

    // ---------------------------------------------------------------- 20.8
    static <T extends Comparable<? super T>> T max(List<? extends T> xs) {
        T best = xs.get(0);
        for (T x : xs) if (x.compareTo(best) > 0) best = x;
        return best;
    }

    static double sumAll(List<? extends Number> producer) {
        double s = 0; for (Number n : producer) s += n.doubleValue(); return s;
    }

    static void fill(List<? super Integer> consumer) { consumer.add(1); consumer.add(2); }

    static void generics() {
        eq(new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass(), true, "erasure: same runtime class");
        eq(max(List.of(3, 9, 4)), 9, "bounded type parameter");
        eq(sumAll(List.of(1, 2.5)), 3.5, "? extends = producer (read)");
        List<Number> sink = new ArrayList<>();
        fill(sink);
        eq(sink, List.of(1, 2), "? super = consumer (write)");
        List<Object> objs = new ArrayList<>();
        fill(objs);
        eq(objs.size(), 2, "? super Integer accepts List<Object>");
        Map<String, List<Integer>> diamond = new HashMap<>();
        eq(diamond.isEmpty(), true, "diamond infers type arguments");
        @SuppressWarnings({"unchecked", "rawtypes"})
        List raw = new ArrayList<String>();
        raw.add(42);                                     // heap pollution: compiles with a warning
        eq(raw.get(0), 42, "raw types bypass checking");
    }

    // ---------------------------------------------------------------- 20.9
    static void collections() {
        throwsA(UnsupportedOperationException.class, () -> List.of(1).add(2), "List.of is unmodifiable");
        throwsA(NullPointerException.class, () -> List.of((Object) null), "List.of rejects null");
        throwsA(IllegalArgumentException.class, () -> Set.of("a", "a"), "Set.of rejects duplicates");
        List<Integer> base = new ArrayList<>(List.of(1));
        List<Integer> view = Collections.unmodifiableList(base), snap = List.copyOf(base);
        base.add(2);
        eq(view.size() + "," + snap.size(), "2,1", "view sees later changes, copy doesn't");
        Integer[] backing = {1, 2};
        Arrays.asList(backing).set(0, 9);
        eq(backing[0], 9, "Arrays.asList writes through to the array");
        Map<String, Integer> hm = new HashMap<>();
        hm.put(null, 0);
        eq(hm.get(null), 0, "HashMap allows one null key");
        throwsA(NullPointerException.class, () -> new ConcurrentHashMap<String, Integer>().put("k", null), "CHM rejects null");
        throwsA(NullPointerException.class, () -> new TreeMap<String, Integer>().put(null, 1), "TreeMap rejects null key");
        throwsA(NullPointerException.class, () -> new ArrayDeque<String>().push(null), "ArrayDeque rejects null");
        eq(hm.put("a", 1), null, "put returns previous value (null)");
        eq(hm.put("a", 2), 1, "put returns previous value");
        eq(hm.getOrDefault("zz", -1), -1, "getOrDefault");
        Map<String, Integer> counts = new TreeMap<>();
        for (String w : "b a b c b".split(" ")) counts.merge(w, 1, Integer::sum);
        eq(counts.toString(), "{a=1, b=3, c=1}", "merge counting; TreeMap is sorted");
        TreeMap<Integer, String> tm = new TreeMap<>(Map.of(10, "x", 20, "y", 30, "z"));
        eq(tm.floorKey(25) + "," + tm.ceilingKey(25) + "," + tm.floorKey(5), "20,30,null", "floor/ceiling");
        eq(tm.headMap(20).keySet() + " " + tm.tailMap(20).keySet(), "[10] [20, 30]", "headMap exclusive, tailMap inclusive");
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(List.of(5, 1, 4));
        eq(minHeap.poll(), 1, "PriorityQueue is a min-heap");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        maxHeap.addAll(List.of(5, 1, 4));
        eq(maxHeap.poll(), 5, "reverseOrder -> max-heap");
        ArrayDeque<Integer> dq = new ArrayDeque<>();
        dq.push(1); dq.push(2); dq.offer(3);
        eq(dq.toString(), "[2, 1, 3]", "push = addFirst, offer = addLast");
        eq(dq.pop(), 2, "stack pop from the head");
        eq(new LinkedHashSet<>(List.of("c", "a", "b")).toString(), "[c, a, b]", "LinkedHashSet keeps insertion order");
        eq(new TreeSet<>(List.of("c", "a", "b")).toString(), "[a, b, c]", "TreeSet sorts");
        LinkedHashMap<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, Integer> e) { return size() > 2; }
        };
        lru.put("a", 1); lru.put("b", 2); lru.get("a"); lru.put("c", 3);
        eq(lru.keySet().toString(), "[a, c]", "LRU via access-ordered LinkedHashMap");
        List<Integer> ls = new ArrayList<>(List.of(1, 2, 3));
        throwsA(ConcurrentModificationException.class, () -> { for (Integer n : ls) if (n == 1) ls.remove(n); },
                "fail-fast iterator");
        List<Integer> ls2 = new ArrayList<>(List.of(1, 2, 3, 4));
        ls2.removeIf(n -> n % 2 == 0);
        eq(ls2, List.of(1, 3), "removeIf is safe");
        Iterator<Integer> it = ls2.iterator(); it.next(); it.remove();
        eq(ls2, List.of(3), "Iterator.remove is safe");
        List<String> seq = new ArrayList<>(List.of("x", "y", "z"));
        eq(seq.getFirst() + seq.getLast() + seq.reversed(), "xz[z, y, x]", "SequencedCollection (21)");
        List<String> words = new ArrayList<>(List.of("pear", "fig", "apple", "kiwi"));
        words.sort(java.util.Comparator.comparing(String::length).thenComparing(java.util.Comparator.reverseOrder()));
        eq(words, List.of("fig", "pear", "kiwi", "apple"), "comparing + thenComparing");
        eq(Collections.frequency(List.of(1, 2, 1), 1), 2, "Collections.frequency");
    }

    // ---------------------------------------------------------------- 20.10
    static void functional() {
        Function<Integer, Integer> plus1 = x -> x + 1, times2 = x -> x * 2;
        eq(plus1.andThen(times2).apply(3), 8, "andThen: first plus1, then times2");
        eq(plus1.compose(times2).apply(3), 7, "compose: first times2, then plus1");
        Predicate<String> empty = String::isEmpty;
        eq(empty.negate().and(s -> s.length() < 3).test("ab"), true, "Predicate negate/and");
        eq(Predicate.not(String::isBlank).test(" "), false, "Predicate.not (11)");
        Supplier<List<String>> make = ArrayList::new;
        eq(make.get().size(), 0, "constructor reference");
        UnaryOperator<String> up = String::toUpperCase;
        eq(up.apply("hi"), "HI", "unbound instance method reference");
        BinaryOperator<Integer> add = Integer::sum;
        eq(add.apply(2, 3), 5, "static method reference");
        eq(Optional.ofNullable(null).map(Object::toString).orElse("none"), "none", "Optional chain");
        eq(Optional.of("x").map(String::toUpperCase).orElseThrow(), "X", "orElseThrow");
        throwsA(NoSuchElementException.class, () -> Optional.empty().get(), "get on empty throws");
        throwsA(NullPointerException.class, () -> Optional.of(null), "Optional.of(null) throws");
        int[] calls = {0};
        Supplier<String> costly = () -> { calls[0]++; return "computed"; };
        Optional.of("present").orElse(costly.get());
        Optional.of("present").orElseGet(costly);
        eq(calls[0], 1, "orElse always evaluates its argument; orElseGet doesn't");
    }

    // ---------------------------------------------------------------- 20.11
    record Emp(String name, String dept, int salary) {}

    static void streams() {
        List<Emp> emps = List.of(new Emp("Ann", "eng", 120), new Emp("Bob", "eng", 100),
                new Emp("Cid", "ops", 90), new Emp("Dee", "ops", 95), new Emp("Eve", "hr", 80));
        Map<String, Long> byDept = emps.stream().collect(Collectors.groupingBy(Emp::dept, TreeMap::new, Collectors.counting()));
        eq(byDept.toString(), "{eng=2, hr=1, ops=2}", "groupingBy + counting");
        Map<Boolean, List<String>> rich = emps.stream().collect(Collectors.partitioningBy(e -> e.salary() >= 100,
                Collectors.mapping(Emp::name, Collectors.toList())));
        eq(rich.toString(), "{false=[Cid, Dee, Eve], true=[Ann, Bob]}", "partitioningBy has both keys");
        eq(Stream.<String>empty().collect(Collectors.partitioningBy(s -> true)).keySet(), Set.of(false, true),
                "partitioningBy on empty still has both keys");
        throwsA(IllegalStateException.class, () -> emps.stream().collect(Collectors.toMap(Emp::dept, Emp::name)),
                "toMap duplicate key throws");
        Map<String, Integer> top = emps.stream().collect(Collectors.toMap(Emp::dept, Emp::salary, Math::max, TreeMap::new));
        eq(top.toString(), "{eng=120, hr=80, ops=95}", "toMap with merge function");
        eq(emps.stream().map(Emp::name).collect(Collectors.joining(", ", "[", "]")), "[Ann, Bob, Cid, Dee, Eve]", "joining");
        eq(emps.stream().mapToInt(Emp::salary).sum(), 485, "IntStream.sum");
        eq(emps.stream().mapToInt(Emp::salary).average().orElse(0), 97.0, "average returns OptionalDouble");
        eq(IntStream.rangeClosed(1, 5).reduce(1, (a, b) -> a * b), 120, "reduce with identity");
        eq(Stream.of(List.of(1, 2), List.of(3)).flatMap(List::stream).toList(), List.of(1, 2, 3), "flatMap");
        eq(Stream.<Integer>empty().anyMatch(x -> true) + " " + Stream.<Integer>empty().allMatch(x -> false),
                "false true", "anyMatch/allMatch on empty");
        List<String> trace = new ArrayList<>();
        Optional<Integer> first = Stream.of(1, 2, 3, 4).peek(x -> trace.add("saw " + x)).filter(x -> x > 1).findFirst();
        eq(first.get() + " " + trace, "2 [saw 1, saw 2]", "lazy + short-circuit: only 2 elements pulled");
        Stream<Integer> once = Stream.of(1);
        once.count();
        throwsA(IllegalStateException.class, once::count, "a stream can be consumed only once");
        throwsA(UnsupportedOperationException.class, () -> Stream.of(1).toList().add(2), "Stream.toList() is unmodifiable");
        eq(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowFixed(2)).toList().toString(), "[[1, 2], [3, 4], [5]]",
                "Gatherers.windowFixed (24)");
        eq(Stream.of(1, 2, 3).gather(Gatherers.scan(() -> 0, Integer::sum)).toList(), List.of(1, 3, 6), "Gatherers.scan");
        eq(Stream.of(5, 1, 7, 2).takeWhile(x -> x < 6).toList(), List.of(5, 1), "takeWhile stops at first failure");
        eq(Stream.iterate(1, x -> x <= 100, x -> x * 10).toList(), List.of(1, 10, 100), "3-arg iterate");
    }

    // ---------------------------------------------------------------- 20.12
    static void io() {
        try {
            eq(Charset.defaultCharset(), StandardCharsets.UTF_8, "UTF-8 by default (18)");
            eq(Path.of("/a/b/../c/./d.txt").normalize().toString(), "/a/c/d.txt", "normalize");
            eq(Path.of("/a/b").relativize(Path.of("/a/c/d")).toString(), "../c/d", "relativize");
            eq(Path.of("/a").resolve("b.txt").toString(), "/a/b.txt", "resolve");
            eq(Path.of("dir/report.csv").getFileName().toString(), "report.csv", "getFileName");
            Path dir = Files.createTempDirectory("ch20");
            Path f = dir.resolve("notes.txt");
            Files.writeString(f, "line1\nline2\nünï\n");
            eq(Files.readAllLines(f).size(), 3, "readAllLines");
            eq(Files.size(f), 18L, "size in bytes: ü and ï take 2 bytes each in UTF-8");
            try (Stream<String> lines = Files.lines(f)) {
                eq(lines.filter(l -> l.startsWith("line")).count(), 2L, "Files.lines is lazy; close it");
            }
            throwsA(java.io.UncheckedIOException.class, () -> {
                try { Files.createFile(f); } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            }, "createFile on an existing file fails");
            eq(Files.deleteIfExists(f) && Files.deleteIfExists(dir), true, "cleanup");
            eq(Files.exists(dir), false, "deleted");
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    // ---------------------------------------------------------------- 20.13
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    static void concurrency() {
        Thread t = new Thread(() -> {});
        eq(t.getState(), Thread.State.NEW, "a thread starts NEW");
        eq(Thread.ofVirtual().unstarted(() -> {}).isVirtual(), true, "Thread.ofVirtual (21)");
        eq(Thread.ofVirtual().unstarted(() -> {}).isDaemon(), true, "virtual threads are always daemon");
        AtomicInteger atomic = new AtomicInteger();
        int[] plain = {0};
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {   // AutoCloseable since 19
            for (int i = 0; i < 1000; i++) pool.submit(() -> { atomic.incrementAndGet(); });
        }                                                   // close() waits for all tasks
        eq(atomic.get(), 1000, "AtomicInteger: no lost updates");
        eq(atomic.compareAndSet(1000, 0), true, "CAS succeeds when the value matches");
        eq(atomic.compareAndSet(1000, 5), false, "CAS fails when it doesn't");
        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 400; i++) pool.submit(() -> chm.merge("k", 1, Integer::sum));
        }
        eq(chm.get("k"), 400, "ConcurrentHashMap.merge is atomic");
        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> qty = CompletableFuture.supplyAsync(() -> 3);
        eq(price.thenCombine(qty, (p, q) -> p * q).join(), 60, "thenCombine");
        eq(CompletableFuture.supplyAsync(() -> 5).thenCompose(x -> CompletableFuture.supplyAsync(() -> x + 1)).join(), 6,
                "thenCompose flattens");
        eq(CompletableFuture.<Integer>failedFuture(new RuntimeException("x")).exceptionally(ex -> -1).join(), -1,
                "exceptionally recovers");
        throwsA(java.util.concurrent.CompletionException.class,
                () -> CompletableFuture.supplyAsync(() -> { throw new IllegalStateException(); }).join(),
                "join wraps failures in CompletionException");
        String seen = ScopedValue.where(USER, "alice").call(() -> USER.get());
        eq(seen, "alice", "ScopedValue bound for the call (25)");
        eq(USER.isBound(), false, "unbound again after the call");
    }

    // ---------------------------------------------------------------- 20.14
    static void jvm() {
        eq(Runtime.version().feature() >= 25, true, "running on JDK 25+");
        eq(String.class.getClassLoader(), null, "bootstrap loader is shown as null");
        eq(CheatsheetChecks.class.getClassLoader().getName(), "app", "your classes: application loader");
        eq(java.sql.Connection.class.getClassLoader().getName(), "platform", "java.sql: platform loader");
        eq(String.class.getModule().getName(), "java.base", "String lives in java.base");
        int[] depth = {0};
        class Deep { void go() { depth[0]++; go(); } }
        throwsA(StackOverflowError.class, () -> new Deep().go(), "unbounded recursion -> StackOverflowError");
        yes(depth[0] > 1000, "stack held more than 1000 frames");
        Runtime rt = Runtime.getRuntime();
        yes(rt.maxMemory() >= rt.totalMemory() && rt.totalMemory() >= rt.freeMemory(), "max >= total >= free");
        yes(!java.lang.management.ManagementFactory.getGarbageCollectorMXBeans().isEmpty(), "GC MXBeans visible");
    }

    // ---------------------------------------------------------------- 20.15
    static void modernJava() {
        record Pair(String k, int v) {}                    // local record (16)
        Object o = new Pair("a", 1);
        String r = o instanceof Pair(String k, int v) && v > 0 ? k : "?";   // record pattern (21)
        eq(r, "a", "record pattern in instanceof");
        int total = 0;
        for (var _ : List.of(1, 2, 3)) total++;             // unnamed variable (22)
        eq(total, 3, "unnamed variable _ (22)");
        var m = new LinkedHashMap<String, Integer>();
        m.put("x", 1); m.put("y", 2);
        eq(m.firstEntry().getKey() + m.lastEntry().getKey(), "xy", "SequencedMap firstEntry/lastEntry (21)");
        eq(m.reversed().keySet().toString(), "[y, x]", "SequencedMap.reversed (21)");
        eq(Stream.of(1, 2, 3, 4).gather(Gatherers.windowSliding(3)).toList().size(), 2, "windowSliding (24)");
        eq("a\\tb".translateEscapes(), "a\tb", "String.translateEscapes (15)");
        eq("hello".transform(s -> s + "!"), "hello!", "String.transform (12)");
        eq(Math.powExact(2, 10), 1024, "Math.powExact (25)");
        throwsA(ArithmeticException.class, () -> Math.powExact(2, 31), "powExact overflow");
    }

    public static void main(String[] args) {
        sheet("20.1 syntax, types, primitives", CheatsheetChecks::syntaxAndTypes);
        sheet("20.2 operators & precedence", CheatsheetChecks::operators);
        sheet("20.3 control flow & patterns", CheatsheetChecks::controlFlow);
        sheet("20.4 methods & arrays", CheatsheetChecks::methodsAndArrays);
        sheet("20.5 OOP", CheatsheetChecks::oop);
        sheet("20.6 core APIs", CheatsheetChecks::coreApis);
        sheet("20.7 exceptions", CheatsheetChecks::exceptions);
        sheet("20.8 generics", CheatsheetChecks::generics);
        sheet("20.9 collections", CheatsheetChecks::collections);
        sheet("20.10 functional interfaces & Optional", CheatsheetChecks::functional);
        sheet("20.11 streams & collectors", CheatsheetChecks::streams);
        sheet("20.12 I/O & NIO.2", CheatsheetChecks::io);
        sheet("20.13 concurrency", CheatsheetChecks::concurrency);
        sheet("20.14 JVM", CheatsheetChecks::jvm);
        sheet("20.15 modern Java 17-25", CheatsheetChecks::modernJava);
        System.out.println("all " + total + " checks passed on Java " + Runtime.version().feature());
    }
}
