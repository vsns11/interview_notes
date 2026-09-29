package ch02.conversions;

/** Widening, narrowing, casting, numeric promotion, compound assignment. */
public class Conversions {
    public static void main(String[] args) {
        int i = 100;
        long l = i;               // widening: automatic, safe
        double d = l;             // widening
        System.out.println("widened: " + l + " " + d);

        double pi = 3.99;
        int truncated = (int) pi;          // narrowing needs a cast; drops the fraction
        long rounded = Math.round(pi);     // rounding is a method, not a cast
        System.out.println("(int) 3.99 = " + truncated + ", Math.round(3.99) = " + rounded);

        int big = 300;
        byte b = (byte) big;               // keeps only the low 8 bits: 300 - 256 = 44
        System.out.println("(byte) 300 = " + b + ", (byte) 200 = " + (byte) 200);
        System.out.println("(int) 1e20 = " + (int) 1e20 + ", (int) Double.NaN = " + (int) Double.NaN);

        int precise = 123_456_789;
        float f = precise;                 // widening but LOSSY: float has 24 bits of precision
        System.out.println("int 123456789 -> float -> int: " + (int) f);

        byte x = 10, y = 20;
        // byte sum = x + y;               // compile error: x + y is an int
        byte sum = (byte) (x + y);
        int sum2 = x + y;                  // numeric promotion: byte + byte -> int
        x += 5;                            // compound assignment hides a cast: x = (byte) (x + 5)
        System.out.println("sum=" + sum + " sum2=" + sum2 + " x=" + x);

        final byte constant = 10 + 20;     // OK: compile-time constant that fits in a byte
        char c = 'a';
        int code = c + 1;                  // char promoted to int
        System.out.println("constant=" + constant + " 'a'+1=" + code + " 7/2=" + 7 / 2 + " 7/2.0=" + 7 / 2.0);
        long wrong = 1_000_000 * 1_000_000;       // int overflow BEFORE widening to long
        long right = 1_000_000L * 1_000_000;
        System.out.println("wrong=" + wrong + " right=" + right);
    }
}
