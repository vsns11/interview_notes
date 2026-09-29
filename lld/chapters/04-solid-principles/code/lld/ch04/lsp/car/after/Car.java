package lld.ch04.lsp.car.after;

public class Car extends Vehicle {
    @Override
    public void replenishEnergy() {
        System.out.println("Car is pumping gasoline");
    }
}
