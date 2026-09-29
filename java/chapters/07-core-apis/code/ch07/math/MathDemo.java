package ch07.math;

public class MathDemo {
    public static void main(String[] args) {
        System.out.println("abs(-7)=" + Math.abs(-7) + " max(3,9)=" + Math.max(3, 9) + " min(2.5,-1)=" + Math.min(2.5, -1));
        System.out.println("pow(2,10)=" + Math.pow(2, 10) + " sqrt(144)=" + Math.sqrt(144) + " cbrt(27)=" + Math.cbrt(27)
                + " hypot(3,4)=" + Math.hypot(3, 4));
        System.out.println("round(2.5)=" + Math.round(2.5) + " round(-2.5)=" + Math.round(-2.5) + " rint(2.5)=" + Math.rint(2.5)
                + " floor(-1.1)=" + Math.floor(-1.1) + " ceil(-1.1)=" + Math.ceil(-1.1));
        System.out.println("7/2=" + (7 / 2) + " -7/2=" + (-7 / 2) + " floorDiv(-7,2)=" + Math.floorDiv(-7, 2)
                + " ceilDiv(7,2)=" + Math.ceilDiv(7, 2) + " floorMod(-7,3)=" + Math.floorMod(-7, 3));   // ceilDiv: Java 18
        System.out.println("clamp(150,0,100)=" + Math.clamp(150, 0, 100) + " clamp(-3.5,0.0,1.0)=" + Math.clamp(-3.5, 0.0, 1.0)); // Java 21
        System.out.println("signum(-4.2)=" + Math.signum(-4.2) + " log10(1000)=" + Math.log10(1000) + " exp(1)=" + Math.exp(1));
        System.out.println("PI=" + Math.PI + " sin(PI/2)=" + Math.sin(Math.PI / 2) + " toDegrees(PI)=" + Math.toDegrees(Math.PI));
        try {
            Math.multiplyExact(1 << 20, 1 << 12);
        } catch (ArithmeticException e) {
            System.out.println("multiplyExact overflow -> ArithmeticException: " + e.getMessage());
        }
        // Pages of results: how many pages of 20 items for 101 items?
        int items = 101, perPage = 20;
        System.out.println("pages=" + Math.ceilDiv(items, perPage) + " (same as (items + perPage - 1) / perPage = "
                + (items + perPage - 1) / perPage + ")");
        double r = Math.random();                          // [0.0, 1.0)
        System.out.println("Math.random() in [0,1)? " + (r >= 0 && r < 1));
    }
}
