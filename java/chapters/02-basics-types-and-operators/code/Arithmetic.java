package ch02.operators;

/** Arithmetic, increment/decrement, division and remainder. */
public class Arithmetic {
    public static void main(String[] args) {
        System.out.println("17 / 5 = " + 17 / 5 + ", 17 % 5 = " + 17 % 5);
        System.out.println("-17 / 5 = " + -17 / 5 + ", -17 % 5 = " + -17 % 5
                + ", Math.floorMod(-17, 5) = " + Math.floorMod(-17, 5));
        System.out.println("17.0 / 5 = " + 17.0 / 5 + ", 5.5 % 2 = " + 5.5 % 2);
        System.out.println("1.0 / 0 = " + 1.0 / 0 + ", 0.0 / 0 = " + 0.0 / 0);
        try {
            System.out.println(1 / 0);
        } catch (ArithmeticException e) {
            System.out.println("1 / 0 -> ArithmeticException: " + e.getMessage());
        }

        int a = 5;
        int b = a++;     // post-increment: b gets the old value, then a becomes 6
        int c = ++a;     // pre-increment: a becomes 7, then c gets 7
        System.out.println("a=" + a + " b=" + b + " c=" + c);

        int n = 3;
        int result = n++ + ++n;   // 3 + 5 (n is 4 after the first operand, 5 after the second)
        System.out.println("n++ + ++n with n=3 -> " + result + ", n=" + n);

        int x = 10;
        x += 3; x -= 1; x *= 2; x /= 5; x %= 3;
        System.out.println("compound ops -> x=" + x);
        System.out.println("-(-5) = " + -(-5) + ", +3 = " + +3);
    }
}
