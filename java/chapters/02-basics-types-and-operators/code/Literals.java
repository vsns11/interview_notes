package ch02.types;

/** Every kind of literal. */
public class Literals {
    public static void main(String[] args) {
        int decimal = 255, hex = 0xFF, octal = 0377, binary = 0b1111_1111;
        long big = 9_000_000_000L;              // L suffix: needed beyond int range
        float price = 19.99f;                   // f suffix: literal is double otherwise
        double sci = 6.022e23, plain = 3.14;
        char letter = 'A', unicode = 'é', tab = '\t';
        boolean ready = true;
        String text = "He said \"hi\"\\n";      // escaped quote and backslash

        System.out.println(decimal + " " + hex + " " + octal + " " + binary);
        System.out.println(big + " " + price + " " + sci + " " + plain);
        System.out.println(letter + " " + unicode + " [" + tab + "] " + ready);
        System.out.println(text);
        System.out.println("char 'A' as a number: " + (int) letter + ", 'A' + 1 = " + (letter + 1)
                + ", (char) ('A' + 1) = " + (char) (letter + 1));
        System.out.println("Integer.toBinaryString(10) = " + Integer.toBinaryString(10)
                + ", toHexString(255) = " + Integer.toHexString(255));
    }
}
