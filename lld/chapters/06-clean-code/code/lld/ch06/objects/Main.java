package lld.ch06.objects;

import java.util.List;

/**
 * Objects vs data structures.
 * Objects hide data and expose behaviour; data structures expose data and have no behaviour.
 * Data/object anti-symmetry: each makes a different kind of change easy.
 */
public class Main {

    // ---------------- STYLE 1: data structures + procedures (procedural) ----------------
    static class Procedural {
        sealed interface Shape permits Square, Circle {}
        record Square(double side) implements Shape {}
        record Circle(double radius) implements Shape {}

        /** All behaviour lives here. Adding perimeter() = one new method, shapes untouched. Adding Triangle = edit every method. */
        static final class Geometry {
            static double area(Shape s) {
                return switch (s) {
                    case Square sq -> sq.side() * sq.side();
                    case Circle c -> Math.PI * c.radius() * c.radius();
                };
            }
            static double perimeter(Shape s) {                 // NEW FUNCTION: easy -- no shape class changes
                return switch (s) {
                    case Square sq -> 4 * sq.side();
                    case Circle c -> 2 * Math.PI * c.radius();
                };
            }
        }
    }

    // ---------------- STYLE 2: objects (polymorphic) ----------------
    static class ObjectOriented {
        interface Shape { double area(); }
        record Square(double side) implements Shape { public double area() { return side * side; } }
        record Circle(double radius) implements Shape { public double area() { return Math.PI * radius * radius; } }
        record Triangle(double base, double height) implements Shape {   // NEW TYPE: easy -- nothing else changes
            public double area() { return 0.5 * base * height; }
        }
        // ...but adding perimeter() would mean editing Shape and EVERY implementation.
    }

    // ---------------- Hiding structure (Law of Demeter) ----------------
    static class Before {
        record Address(String city, String pincode) {}
        static class Customer { Address address = new Address("Pune", "411001"); Address getAddress() { return address; } }
        static class Order { Customer customer = new Customer(); Customer getCustomer() { return customer; } }
        static boolean isMetroDelivery(Order o) {
            return List.of("Mumbai", "Delhi", "Pune").contains(o.getCustomer().getAddress().city());  // train wreck
        }
    }
    static class After {
        record Address(String city, String pincode) {                    // a data structure: exposing data is its job
            boolean isMetro() { return List.of("Mumbai", "Delhi", "Pune").contains(city); }
        }
        static final class Order {                                         // an object: hides its structure
            private final Address shipTo;
            Order(Address shipTo) { this.shipTo = shipTo; }
            boolean isMetroDelivery() { return shipTo.isMetro(); }        // tell the order what you need
        }
    }

    // ---------------- DTO: pure data for crossing a boundary ----------------
    /** A Data Transfer Object: public data, no behaviour; e.g. JSON returned by a REST endpoint. */
    record OrderDto(String orderId, String city, long totalPaise) {}

    public static void main(String[] args) {
        var shapes = List.<Procedural.Shape>of(new Procedural.Square(2), new Procedural.Circle(1));
        for (var s : shapes)
            System.out.printf("procedural %-22s area=%.2f perimeter=%.2f%n", s, Procedural.Geometry.area(s), Procedural.Geometry.perimeter(s));

        var objects = List.<ObjectOriented.Shape>of(new ObjectOriented.Square(2), new ObjectOriented.Circle(1),
                new ObjectOriented.Triangle(3, 4));
        for (var s : objects) System.out.printf("object     %-22s area=%.2f%n", s, s.area());

        System.out.println("BEFORE train wreck says metro? " + Before.isMetroDelivery(new Before.Order()));
        System.out.println("AFTER  order.isMetroDelivery()? " + new After.Order(new After.Address("Pune", "411001")).isMetroDelivery());
        System.out.println("DTO: " + new OrderDto("o-9", "Pune", 45_000));
    }
}
