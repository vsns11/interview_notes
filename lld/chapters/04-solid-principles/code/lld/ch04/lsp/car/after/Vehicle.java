package lld.ch04.lsp.car.after;

/** The base type promises the GOAL every vehicle shares, not one mechanism. */
public abstract class Vehicle {
    public void start() {
        System.out.println(getClass().getSimpleName() + " is starting");
    }

    public void stop() {
        System.out.println(getClass().getSimpleName() + " is stopping");
    }

    public abstract void replenishEnergy();
}
