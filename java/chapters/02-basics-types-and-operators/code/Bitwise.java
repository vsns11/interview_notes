package ch02.operators;

/** Bitwise and shift operators, with a real use: flags. */
public class Bitwise {
    static final int READ = 1, WRITE = 2, EXEC = 4;   // 001, 010, 100

    public static void main(String[] args) {
        int a = 0b1100, b = 0b1010;
        System.out.println("a & b = " + bin(a & b) + ", a | b = " + bin(a | b)
                + ", a ^ b = " + bin(a ^ b) + ", ~a = " + (~a));
        System.out.println("1 << 4 = " + (1 << 4) + ", 64 >> 2 = " + (64 >> 2));
        System.out.println("-16 >> 2 = " + (-16 >> 2) + ", -16 >>> 28 = " + (-16 >>> 28));
        System.out.println("1 << 32 = " + (1 << 32) + "  (shift distance is taken mod 32 for int)");

        int perms = READ | WRITE;                 // set two flags
        System.out.println("can write? " + ((perms & WRITE) != 0) + ", can exec? " + ((perms & EXEC) != 0));
        perms &= ~WRITE;                          // clear a flag
        System.out.println("after clearing WRITE: " + bin(perms));
        System.out.println("is 12 even? " + ((12 & 1) == 0));
    }

    static String bin(int v) { return Integer.toBinaryString(v); }
}
