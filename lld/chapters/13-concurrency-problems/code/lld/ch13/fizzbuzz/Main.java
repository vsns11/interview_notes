package lld.ch13.fizzbuzz;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/**
 * LeetCode 1195 - Fizz Buzz Multithreaded.
 * Four threads share one counter 1..n: fizz() prints "fizz" (divisible by 3 only), buzz() "buzz"
 * (by 5 only), fizzbuzz() "fizzbuzz" (by 15), number() prints the number otherwise.
 */
public class Main {

    static class FizzBuzz {
        private final int n;
        private int current = 1;                              // guarded by 'this'

        FizzBuzz(int n) { this.n = n; }

        /** Shared loop: wait until 'current' is mine (or we're done), act, advance, wake everyone. */
        private void runWhile(IntPredicate mine, IntConsumer action) throws InterruptedException {
            while (true) {
                synchronized (this) {
                    while (current <= n && !mine.test(current)) wait();
                    if (current > n) { notifyAll(); return; }     // done: wake others so they can exit too
                    action.accept(current);
                    current++;
                    notifyAll();                                  // the next number may belong to anyone
                }
            }
        }

        void fizz(Runnable printFizz) throws InterruptedException {
            runWhile(i -> i % 3 == 0 && i % 5 != 0, i -> printFizz.run());
        }
        void buzz(Runnable printBuzz) throws InterruptedException {
            runWhile(i -> i % 5 == 0 && i % 3 != 0, i -> printBuzz.run());
        }
        void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
            runWhile(i -> i % 15 == 0, i -> printFizzBuzz.run());
        }
        void number(IntConsumer printNumber) throws InterruptedException {
            runWhile(i -> i % 3 != 0 && i % 5 != 0, printNumber);
        }
    }

    interface Body { void run() throws InterruptedException; }
    static Thread start(Body b) {
        Thread t = new Thread(() -> { try { b.run(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        t.start();
        return t;
    }

    public static void main(String[] args) throws InterruptedException {
        int n = 15;
        List<String> out = new ArrayList<>();          // only touched inside the FizzBuzz monitor
        FizzBuzz fb = new FizzBuzz(n);
        List<Thread> ts = List.of(
                start(() -> fb.fizz(() -> out.add("fizz"))),
                start(() -> fb.buzz(() -> out.add("buzz"))),
                start(() -> fb.fizzbuzz(() -> out.add("fizzbuzz"))),
                start(() -> fb.number(i -> out.add(String.valueOf(i)))));
        for (Thread t : ts) t.join();                  // join() makes the list's contents visible to main
        System.out.println(String.join(", ", out));
    }
}
