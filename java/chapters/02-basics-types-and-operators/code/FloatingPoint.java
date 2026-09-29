package ch02.numbers;

import java.math.BigDecimal;

/** IEEE 754 surprises and how to handle them. */
public class FloatingPoint {
    public static void main(String[] args) {
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2));
        System.out.println("0.1 + 0.2 == 0.3: " + (0.1 + 0.2 == 0.3));
        System.out.println("|diff| < 1e-9: " + (Math.abs((0.1 + 0.2) - 0.3) < 1e-9));
        System.out.println("0.1f + 0.2f = " + (0.1f + 0.2f));
        System.out.println("1.0 - 0.9 = " + (1.0 - 0.9));

        double nan = 0.0 / 0;
        System.out.println("NaN == NaN: " + (nan == nan) + ", Double.isNaN: " + Double.isNaN(nan));
        System.out.println("-0.0 == 0.0: " + (-0.0 == 0.0) + ", Double.compare(-0.0, 0.0): " + Double.compare(-0.0, 0.0));
        System.out.println("1e308 * 10 = " + 1e308 * 10 + ", Math.sqrt(-1) = " + Math.sqrt(-1));
        System.out.println("16777217 as float = " + (float) 16_777_217 + "  (2^24 + 1 not representable)");

        double total = 0;
        for (int i = 0; i < 10; i++) total += 0.1;
        System.out.println("0.1 added ten times = " + total);

        BigDecimal exact = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        System.out.println("BigDecimal 0.1 + 0.2 = " + exact
                + ", new BigDecimal(0.1) = " + new BigDecimal(0.1));
        System.out.println("Math.round(2.5)=" + Math.round(2.5) + " Math.round(-2.5)=" + Math.round(-2.5)
                + " Math.floor(-2.5)=" + Math.floor(-2.5) + " Math.ceil(-2.5)=" + Math.ceil(-2.5));
    }
}
