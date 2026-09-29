package ch19.toolkit;

import java.util.*;

/** Overflow, Math.*Exact, integer division/modulo, Integer caching and other bugs that fail hidden tests. */
public class NumericPitfalls {
    public static void main(String[] args) {
        // 1. int overflow wraps silently
        int max = Integer.MAX_VALUE;
        System.out.println("MAX + 1            " + (max + 1));
        int lo = 2_000_000_000, hi = 2_100_000_000;
        System.out.println("(lo+hi)/2          " + (lo + hi) / 2 + "  lo+(hi-lo)/2=" + (lo + (hi - lo) / 2)
                + "  (lo+hi)>>>1=" + ((lo + hi) >>> 1));

        // 2. multiply in long BEFORE the overflow happens
        int n = 100_000;
        long wrong = n * n;                    // int * int overflows, then widens
        long right = (long) n * n;
        System.out.println("n*n wrong/right    " + wrong + " / " + right);

        // 3. Math.*Exact throws instead of wrapping
        try {
            Math.multiplyExact(n, n);
        } catch (ArithmeticException e) {
            System.out.println("multiplyExact      ArithmeticException: " + e.getMessage());
        }
        System.out.println("toIntExact ok      " + Math.toIntExact(42L));

        // 4. abs(MIN_VALUE) is still negative
        System.out.println("abs(MIN_VALUE)     " + Math.abs(Integer.MIN_VALUE));

        // 5. integer division truncates toward zero; % keeps the sign of the dividend
        System.out.println("7/2, -7/2          " + 7 / 2 + ", " + -7 / 2 + "   7/2.0=" + 7 / 2.0);
        System.out.println("-7 % 3, floorMod   " + (-7 % 3) + ", " + Math.floorMod(-7, 3));

        // 6. Integer caching: == compares references; only -128..127 are guaranteed cached
        Integer a = 127, b = 127, c = 128, d = 128;
        System.out.println("127 == 127         " + (a == b));
        System.out.println("128 == 128         " + (c == d) + "   equals=" + c.equals(d));
        Map<String, Integer> m = new HashMap<>(Map.of("x", 1000, "y", 1000));
        System.out.println("map.get == get     " + (m.get("x") == m.get("y"))
                + "   intValue== " + (m.get("x").intValue() == m.get("y").intValue()));

        // 7. list.remove(int index) vs remove(Object value)
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
        list.remove(1);                        // removes INDEX 1 -> [10, 30]
        list.remove(Integer.valueOf(10));      // removes VALUE 10 -> [30]
        System.out.println("remove idx/value   " + list);

        // 8. String == vs equals
        String s1 = "java", s2 = new String("java");
        System.out.println("String ==, equals  " + (s1 == s2) + ", " + s1.equals(s2));

        // 9. char + int is int; char + String is String
        System.out.println("'a' + 1, \"\" + 'a' + 1  " + ('a' + 1) + ", " + ("" + 'a' + 1));

        // 10. modifying a list while for-each iterating it
        List<Integer> xs = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (int x : xs) if (x % 2 == 0) xs.remove(Integer.valueOf(x));
        } catch (ConcurrentModificationException e) {
            System.out.println("for-each remove    ConcurrentModificationException");
        }
        xs.removeIf(x -> x % 2 == 0);
        System.out.println("removeIf           " + xs);
    }
}
