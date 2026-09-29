package lld.ch04.lsp.car.before;

public class Car {
    public void start() {
        System.out.println("Car is starting");
    }

    public void stop() {
        System.out.println("Car is stopping");
    }

    /** Names a MECHANISM (liquid fuel) instead of a goal. */
    public void refuel() {
        System.out.println("Car is refueling");
    }
}
