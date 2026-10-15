package ch22.barbershop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 3 waiting chairs, a haircut takes 230 ms, customers C1..C8 arrive every 50 ms,
 * then C9 arrives at 1400 ms when the barber is asleep again.
 * Every event is at least 20 ms away from every other, so the printed order is stable.
 */
public class Main {
    static final long START = System.nanoTime();

    static void sleepUntil(long ms) throws InterruptedException {
        long left = ms - (System.nanoTime() - START) / 1_000_000;
        if (left > 0) Thread.sleep(left);
    }

    static synchronized void log(String s) { System.out.println(s); }

    public static void main(String[] args) throws InterruptedException {
        BarberShop shop = new BarberShop(3);
        AtomicInteger served = new AtomicInteger(), turnedAway = new AtomicInteger();

        Thread barber = new Thread(() -> {
            try {
                shop.barberLoop(customer -> {
                    log("barber: cutting " + customer);
                    try { Thread.sleep(230); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    served.incrementAndGet();
                }, () -> log("barber: no customers, sleeping"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "barber");
        barber.start();

        List<Thread> customers = new ArrayList<>();
        long[] arrivals = {20, 70, 120, 170, 220, 270, 320, 370, 1420};
        for (int i = 0; i < arrivals.length; i++) {
            String name = "C" + (i + 1);
            sleepUntil(arrivals[i]);
            log(name + " arrives");
            Thread c = new Thread(() -> {
                try {
                    if (!shop.visit(name)) {
                        turnedAway.incrementAndGet();
                        log(name + " leaves: all chairs taken");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, name);
            customers.add(c);
            c.start();
        }
        for (Thread c : customers) c.join();   // every customer has been served or turned away
        shop.close();
        barber.join();
        log("served=" + served.get() + ", turned away=" + turnedAway.get() + ", barber thread ended: " + !barber.isAlive());
    }
}
