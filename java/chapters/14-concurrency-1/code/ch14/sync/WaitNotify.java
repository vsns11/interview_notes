package ch14.sync;

import java.util.ArrayDeque;
import java.util.Queue;

/** A tiny bounded buffer built with wait/notifyAll - the guarded-block idiom. */
public class WaitNotify {

    static class BoundedBuffer<T> {
        private final Queue<T> items = new ArrayDeque<>();
        private final int capacity;
        BoundedBuffer(int capacity) { this.capacity = capacity; }

        public synchronized void put(T item) throws InterruptedException {
            while (items.size() == capacity) wait();          // ALWAYS a while loop, never if
            items.add(item);
            notifyAll();                                      // wake consumers waiting for "not empty"
        }

        public synchronized T take() throws InterruptedException {
            while (items.isEmpty()) wait();                   // releases the lock while waiting
            T item = items.remove();
            notifyAll();                                      // wake producers waiting for "not full"
            return item;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer<Integer> buf = new BoundedBuffer<>(2);
        int[] sum = {0};
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 10; i++) buf.put(i); buf.put(-1); }  // -1 = "no more" (poison pill)
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = new Thread(() -> {
            try {
                StringBuilder order = new StringBuilder();
                for (int x; (x = buf.take()) != -1; ) { sum[0] += x; order.append(x).append(' '); }
                System.out.println("consumer received in order: " + order.toString().trim());
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();
        System.out.println("sum = " + sum[0] + " (1..10 = 55)");

        try {
            new Object().wait();                              // not holding the monitor
        } catch (IllegalMonitorStateException e) {
            System.out.println("wait() without the lock -> IllegalMonitorStateException");
        }
    }
}
