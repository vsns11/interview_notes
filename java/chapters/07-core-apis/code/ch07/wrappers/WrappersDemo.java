package ch07.wrappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WrappersDemo {
    public static void main(String[] args) {
        // Autoboxing and unboxing
        List<Integer> nums = new ArrayList<>();
        nums.add(5);                           // autobox: Integer.valueOf(5)
        int first = nums.get(0);               // unbox: intValue()
        System.out.println("boxed then unboxed: " + first);

        // The Integer cache: valueOf(-128..127) returns shared objects
        Integer a = 127, b = 127, c = 128, d = 128;
        System.out.println("127 == 127: " + (a == b) + ", 128 == 128: " + (c == d) + ", 128 equals 128: " + c.equals(d));

        // Parsing and converting
        int p = Integer.parseInt("42");                // primitive
        Integer v = Integer.valueOf("42");             // wrapper (may be cached)
        System.out.println("parseInt=" + p + " valueOf=" + v + " parseInt(\"ff\",16)=" + Integer.parseInt("ff", 16)
                + " toBinaryString(10)=" + Integer.toBinaryString(10) + " Double.parseDouble(\"1e3\")=" + Double.parseDouble("1e3")
                + " Boolean.parseBoolean(\"YES\")=" + Boolean.parseBoolean("YES"));
        try {
            Integer.parseInt("4 2");
        } catch (NumberFormatException e) {
            System.out.println("NumberFormatException: " + e.getMessage());
        }

        // Useful constants and static helpers
        System.out.println("Integer.MAX_VALUE=" + Integer.MAX_VALUE + " Integer.compare(3,7)=" + Integer.compare(3, 7)
                + " Integer.sum(2,3)=" + Integer.sum(2, 3) + " Integer.bitCount(255)=" + Integer.bitCount(255)
                + " Character.isDigit('7')=" + Character.isDigit('7') + " Character.toUpperCase('q')=" + Character.toUpperCase('q')
                + " Character.getNumericValue('9')=" + Character.getNumericValue('9'));

        // equals across wrapper types is false
        Long big = 42L;
        System.out.println("Long(42).equals(Integer 42): " + big.equals(42) + ", big == 42: " + (big == 42));

        // Unboxing null -> NullPointerException
        Map<String, Integer> stock = new HashMap<>();
        try {
            int apples = stock.get("apples");              // get returns null, unboxing throws
            System.out.println(apples);
        } catch (NullPointerException e) {
            System.out.println("NPE: " + e.getMessage());
        }
        System.out.println("safe: " + stock.getOrDefault("apples", 0));

        // List.remove(int index) vs remove(Object value)
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
        list.remove(1);                                    // removes INDEX 1 -> [10, 30]
        list.remove(Integer.valueOf(10));                  // removes VALUE 10 -> [30]
        System.out.println("after remove(1) and remove(Integer.valueOf(10)): " + list);

        // Ternary with mixed boxed/primitive operands unboxes
        Integer maybe = null;
        try {
            Integer result = true ? maybe : 0;             // unboxes maybe -> NPE
            System.out.println(result);
        } catch (NullPointerException e) {
            System.out.println("ternary unboxing NPE");
        }

        // Boxing in a hot loop allocates
        Long total = 0L;                                   // should be long
        for (long i = 0; i < 1_000; i++) total += i;       // unbox, add, box again each time
        System.out.println("total=" + total);
    }
}
