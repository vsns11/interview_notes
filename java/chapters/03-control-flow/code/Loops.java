package ch03.loops;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** while, do-while, for, enhanced for. */
public class Loops {
    public static void main(String[] args) {
        int n = 9075, digitSum = 0;
        while (n > 0) {                       // condition checked BEFORE each iteration
            digitSum += n % 10;
            n /= 10;
        }
        System.out.println("digit sum of 9075 = " + digitSum);

        int attempts = 0;
        int[] fakeRolls = {2, 5, 6, 3};
        int roll;
        do {                                  // body runs at least once; condition checked AFTER
            roll = fakeRolls[attempts++];
            System.out.print("rolled " + roll + "; ");
        } while (roll != 6);
        System.out.println("got a 6 after " + attempts + " attempts");

        long factorial = 1;
        for (int i = 1; i <= 10; i++) {       // init; condition; update
            factorial *= i;
        }
        System.out.println("10! = " + factorial);

        for (int i = 0, j = 10; i < j; i += 3, j -= 3) {   // several variables of one type
            System.out.print("(" + i + "," + j + ") ");
        }
        System.out.println();

        for (int i = 10; i > 0; i /= 2) System.out.print(i + " ");   // any update expression
        System.out.println();

        int[] temps = {21, 25, 19, 30};
        int max = Integer.MIN_VALUE;
        for (int t : temps) max = Math.max(max, t);   // enhanced for over an array
        System.out.println("max temp = " + max);

        List<String> names = List.of("Ada", "Grace", "Linus");
        for (String name : names) System.out.print(name.toUpperCase() + " ");   // over any Iterable
        System.out.println();

        Map<String, Integer> stock = new TreeMap<>(Map.of("apple", 3, "pear", 0, "fig", 7));
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            System.out.print(e.getKey() + ":" + e.getValue() + " ");
        }
        System.out.println();

        for (int i = 1; i <= 15; i++) {       // FizzBuzz
            String out = i % 15 == 0 ? "FizzBuzz" : i % 3 == 0 ? "Fizz" : i % 5 == 0 ? "Buzz" : String.valueOf(i);
            System.out.print(out + (i < 15 ? " " : "\n"));
        }
    }
}
