package ch02.operators;

/** Operator precedence and associativity in action. */
public class Precedence {
    public static void main(String[] args) {
        System.out.println("2 + 3 * 4 = " + (2 + 3 * 4));
        System.out.println("(2 + 3) * 4 = " + ((2 + 3) * 4));
        System.out.println("10 - 4 - 3 = " + (10 - 4 - 3) + "  (left to right)");
        int a, b, c;
        a = b = c = 7;                                  // assignment is right-associative
        System.out.println("a=" + a + " b=" + b + " c=" + c);
        System.out.println(1 + 2 + "3" + 4 + 5);            // left to right: 3, then "33", "334", "3345"
        System.out.println("true || false && false = " + (true || false && false));
        System.out.println("5 > 3 == true: " + (5 > 3 == true));
        int x = 6;
        System.out.println("x & 1 == 0 needs parentheses: " + ((x & 1) == 0));
    }
}
