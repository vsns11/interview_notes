package ch14.sync;

/**
 * JLS 17.7: a plain long/double write MAY be split into two 32-bit halves ("tearing");
 * volatile long/double and all reference writes are always atomic.
 * A writer flips between two values whose halves differ; a reader looks for a mixed value.
 */
public class Tearing {
    static long plain;                  // tearing allowed by the JLS
    static volatile long vol;           // tearing forbidden
    static final long A = 0L, B = -1L;  // 0x0000000000000000 and 0xFFFFFFFFFFFFFFFF

    public static void main(String[] args) throws InterruptedException {
        System.out.println("plain long   torn reads: " + count(false)
                + "   [machine-dependent: 64-bit JVMs write a long in one instruction; a 32-bit JVM may tear]");
        System.out.println("volatile long torn reads: " + count(true) + "   (guaranteed 0 by JLS 17.7)");
    }

    static long count(boolean useVolatile) throws InterruptedException {
        final int rounds = 20_000_000;
        Thread writer = new Thread(() -> {
            for (int i = 0; i < rounds; i++) {
                long v = (i & 1) == 0 ? A : B;
                if (useVolatile) vol = v; else plain = v;
            }
        });
        long[] torn = {0};
        Thread reader = new Thread(() -> {
            for (int i = 0; i < rounds; i++) {
                long v = useVolatile ? vol : plain;
                if (v != A && v != B) torn[0]++;          // e.g. 0x00000000FFFFFFFF = one half from each write
            }
        });
        writer.start(); reader.start();
        writer.join(); reader.join();
        return torn[0];
    }
}
