package ch07.big;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class BigNumbers {
    public static void main(String[] args) {
        // BigInteger: arbitrary-precision integers
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= 30; i++) f = f.multiply(BigInteger.valueOf(i));
        System.out.println("30! = " + f + " (" + f.bitLength() + " bits)");
        BigInteger two = BigInteger.TWO;                               // Java 9
        System.out.println("2^100 = " + two.pow(100));
        System.out.println("gcd(84,36)=" + BigInteger.valueOf(84).gcd(BigInteger.valueOf(36))
                + " 3^200 mod 1000=" + BigInteger.valueOf(3).modPow(BigInteger.valueOf(200), BigInteger.valueOf(1000))
                + " isProbablePrime(97)=" + BigInteger.valueOf(97).isProbablePrime(20)
                + " sqrt(10^20)=" + BigInteger.TEN.pow(20).sqrt());
        try {
            f.longValueExact();
        } catch (ArithmeticException e) {
            System.out.println("30!.longValueExact(): " + e.getMessage());
        }

        // BigDecimal: exact decimal arithmetic
        System.out.println("double 0.1+0.2=" + (0.1 + 0.2)
                + " BigDecimal=" + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        System.out.println("new BigDecimal(0.1)=" + new BigDecimal(0.1));
        System.out.println("BigDecimal.valueOf(0.1)=" + BigDecimal.valueOf(0.1));

        BigDecimal x = new BigDecimal("2.0"), y = new BigDecimal("2.00");
        System.out.println("2.0 equals 2.00? " + x.equals(y) + ", compareTo == 0? " + (x.compareTo(y) == 0)
                + ", scales " + x.scale() + " vs " + y.scale() + ", stripTrailingZeros equal? "
                + x.stripTrailingZeros().equals(y.stripTrailingZeros()));

        BigDecimal ten = new BigDecimal("10"), three = new BigDecimal("3");
        try {
            ten.divide(three);
        } catch (ArithmeticException e) {
            System.out.println("10/3: " + e.getMessage());
        }
        System.out.println("10/3 scale 4 HALF_UP=" + ten.divide(three, 4, RoundingMode.HALF_UP)
                + " MathContext.DECIMAL64=" + ten.divide(three, MathContext.DECIMAL64));

        for (String s : new String[]{"2.345", "2.355", "-2.345"}) {
            BigDecimal v = new BigDecimal(s);
            System.out.println(s + " -> HALF_UP " + v.setScale(2, RoundingMode.HALF_UP)
                    + ", HALF_EVEN " + v.setScale(2, RoundingMode.HALF_EVEN)
                    + ", DOWN " + v.setScale(2, RoundingMode.DOWN)
                    + ", FLOOR " + v.setScale(2, RoundingMode.FLOOR));
        }

        // Money example: price * qty + 8.25% tax, rounded to cents
        BigDecimal price = new BigDecimal("19.99");
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(3));
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.0825")).setScale(2, RoundingMode.HALF_EVEN);
        System.out.println("subtotal=" + subtotal + " tax=" + tax + " total=" + subtotal.add(tax));

        // Split $100.00 three ways without losing a cent
        BigDecimal[] qr = new BigDecimal("100.00").divideAndRemainder(new BigDecimal("3"));
        BigDecimal share = new BigDecimal("100.00").divide(new BigDecimal("3"), 2, RoundingMode.DOWN);
        BigDecimal leftover = new BigDecimal("100.00").subtract(share.multiply(BigDecimal.valueOf(3)));
        System.out.println("share=" + share + " leftover cent(s)=" + leftover + " (divideAndRemainder: "
                + qr[0] + " r " + qr[1] + ")");
    }
}
