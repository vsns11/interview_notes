package ch01.jit;

/** Calls one small method millions of times so the JIT compiles it. Run with -XX:+PrintCompilation. */
public class HotLoop {
    static long square(long x) {
        return x * x;
    }

    public static void main(String[] args) {
        long sum = 0;
        for (long i = 0; i < 5_000_000; i++) {
            sum += square(i % 1000);
        }
        System.out.println("sum = " + sum);
    }
}
