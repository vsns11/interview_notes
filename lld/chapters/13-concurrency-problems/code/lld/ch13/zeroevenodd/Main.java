package lld.ch13.zeroevenodd;

import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

/**
 * LeetCode 1116 - Print Zero Even Odd.
 * Three threads: zero() prints only 0s, even() only even numbers, odd() only odd numbers.
 * Together they must print 0 1 0 2 0 3 ... 0 n  (e.g. n=5 -> "0102030405").
 */
public class Main {

    static class ZeroEvenOdd {
        private final int n;
        private final Semaphore zeroTurn = new Semaphore(1);
        private final Semaphore oddTurn = new Semaphore(0);
        private final Semaphore evenTurn = new Semaphore(0);

        ZeroEvenOdd(int n) { this.n = n; }

        void zero(IntConsumer print) throws InterruptedException {
            for (int i = 1; i <= n; i++) {
                zeroTurn.acquire();
                print.accept(0);
                // The zero thread is the "dispatcher": it decides who prints next.
                if (i % 2 == 1) oddTurn.release(); else evenTurn.release();
            }
        }
        void odd(IntConsumer print) throws InterruptedException {
            for (int i = 1; i <= n; i += 2) {
                oddTurn.acquire();
                print.accept(i);
                zeroTurn.release();
            }
        }
        void even(IntConsumer print) throws InterruptedException {
            for (int i = 2; i <= n; i += 2) {
                evenTurn.acquire();
                print.accept(i);
                zeroTurn.release();
            }
        }
    }

    interface Body { void run() throws InterruptedException; }
    static Thread start(Body b) {
        Thread t = new Thread(() -> { try { b.run(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        t.start();
        return t;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int n : new int[] {1, 2, 5, 10}) {
            StringBuffer out = new StringBuffer();
            ZeroEvenOdd z = new ZeroEvenOdd(n);
            Thread e = start(() -> z.even(out::append));
            Thread o = start(() -> z.odd(out::append));
            Thread zt = start(() -> z.zero(out::append));
            zt.join(); o.join(); e.join();
            System.out.println("n=" + n + " -> " + out);
        }
    }
}
