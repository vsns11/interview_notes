package lld.ch04.lsp.car.after;

/** A sibling of Car, not a child of it. Both can stand in for Vehicle. */
public class ElectricCar extends Vehicle {
    @Override
    public void replenishEnergy() {
        System.out.println("Electric car is charging its battery");
    }
}
