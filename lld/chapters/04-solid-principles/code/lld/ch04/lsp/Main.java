package lld.ch04.lsp;

import java.math.BigDecimal;
import java.util.List;
import lld.ch04.lsp.birds.composition.Bird;
import lld.ch04.lsp.birds.composition.Flying;
import lld.ch04.lsp.birds.composition.Running;
import lld.ch04.lsp.birds.composition.Swimming;
import lld.ch04.lsp.car.after.Vehicle;
import lld.ch04.lsp.rules.Account;
import lld.ch04.lsp.rules.AdjustablePrice;
import lld.ch04.lsp.rules.Discount;
import lld.ch04.lsp.rules.OverdraftAccount;
import lld.ch04.lsp.rules.Price;
import lld.ch04.lsp.rules.Rectangle;
import lld.ch04.lsp.rules.SmallOrderDiscount;
import lld.ch04.lsp.rules.Square;
import lld.ch04.lsp.shapes.Rect;
import lld.ch04.lsp.shapes.Shape;
import lld.ch04.lsp.shapes.Sq;

public class Main {

    // ---- Client code: every method below knows ONLY the base type ----

    static void service(lld.ch04.lsp.car.before.Car car) {
        car.start();
        car.refuel();
        car.stop();
    }

    static void takeATrip(Vehicle v) {
        v.start();
        v.replenishEnergy();
        v.stop();
    }

    static void checkout(Discount d, BigDecimal amount) {
        try {
            System.out.println("  Charged: " + d.apply(amount));
        } catch (IllegalArgumentException e) {
            System.out.println("  Broke on a valid amount: " + e.getMessage());
        }
    }

    static void paySupplier(Account account, BigDecimal amount, String label) {
        BigDecimal balance = account.withdraw(amount);
        System.out.println("  " + label + " balance after payment: " + balance
                + (balance.signum() < 0 ? "   <-- the caller never expected this" : ""));
    }

    /** A contract test written against Rectangle -- run it for every subclass. */
    static void resizeAndCheck(Rectangle r, String label) {
        r.setWidth(5);
        r.setHeight(4);
        System.out.println("  " + label + " expected area 20, got " + r.area());
    }

    static void quoteThenCharge(Price p, Runnable somethingElseHappens) {
        BigDecimal quoted = p.value();
        somethingElseHappens.run();
        System.out.println("  quoted " + quoted + ", charged " + p.value());
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Car / ElectricCar (exception rule) ===");
        service(new lld.ch04.lsp.car.before.Car());
        try {
            service(new lld.ch04.lsp.car.before.ElectricCar());
        } catch (UnsupportedOperationException e) {
            System.out.println("  substitution broke: " + e.getMessage());
        }

        System.out.println("=== 2. Fixed hierarchy: Vehicle -> Car | ElectricCar ===");
        List<Vehicle> fleet = List.of(new lld.ch04.lsp.car.after.Car(),
                new lld.ch04.lsp.car.after.ElectricCar());
        fleet.forEach(Main::takeATrip);

        System.out.println("=== 3. Precondition strengthened ===");
        checkout(new Discount(), new BigDecimal("250.00"));
        checkout(new SmallOrderDiscount(), new BigDecimal("250.00"));

        System.out.println("=== 4. Postcondition weakened ===");
        paySupplier(new Account(new BigDecimal("50.00")), new BigDecimal("80.00"), "Account:         ");
        paySupplier(new OverdraftAccount(new BigDecimal("50.00")), new BigDecimal("80.00"),
                "OverdraftAccount:");

        System.out.println("=== 5. Invariant broken (Rectangle / Square) ===");
        resizeAndCheck(new Rectangle(1, 1), "Rectangle:");
        resizeAndCheck(new Square(1), "Square:   ");

        System.out.println("=== 6. History constraint broken ===");
        quoteThenCharge(new Price(new BigDecimal("100.00")), () -> { });
        AdjustablePrice sneaky = new AdjustablePrice(new BigDecimal("100.00"));
        quoteThenCharge(sneaky, () -> sneaky.adjust(new BigDecimal("120.00")));

        System.out.println("=== 7. Fix for shapes: no shared mutable promise ===");
        List<Shape> shapes = List.of(new Rect(5, 4), new Sq(4), new Rect(2, 3).withWidth(5));
        for (Shape s : shapes) {
            System.out.println("  " + s + " area=" + s.area());
        }

        System.out.println("=== 8. Birds: inheritance vs composition ===");
        List<lld.ch04.lsp.birds.inheritance.Bird> old = List.of(
                new lld.ch04.lsp.birds.inheritance.Sparrow(),
                new lld.ch04.lsp.birds.inheritance.Penguin());
        for (var b : old) {
            try {
                b.fly();
            } catch (UnsupportedOperationException e) {
                System.out.println("  Substitution broke: " + e.getMessage());
            }
        }
        List<Bird> birds = List.of(
                new Bird("Sparrow", new Flying()),
                new Bird("Penguin", new Swimming()),
                new Bird("Ostrich", new Running()));
        birds.forEach(Bird::travel);
        Bird sparrow = birds.get(0);
        sparrow.changeMovement(new Running()); // behaviour swapped at run time
        sparrow.travel();
    }
}
