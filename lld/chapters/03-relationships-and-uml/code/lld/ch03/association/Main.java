package lld.ch03.association;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("== Uni-directional: Order -> Customer ==");
        Customer asha = new Customer("Asha");
        Order o1 = new Order("O-1", asha);
        Order o2 = new Order("O-2", asha);
        System.out.println(o1.id() + " belongs to " + o1.customer().name());
        System.out.println(o2.id() + " belongs to " + o2.customer().name());
        System.out.println("Customer has no way to list its orders (one-way link).");

        System.out.println("\n== Bi-directional many-to-many: Student <-> Course ==");
        Student ravi = new Student("Ravi");
        Student meera = new Student("Meera");
        Course java = new Course("JAVA101", 2);
        Course uml = new Course("UML201", 30);
        java.enroll(ravi);
        java.enroll(meera);
        uml.enroll(ravi);
        System.out.println("JAVA101 students: " + java.students());
        System.out.println("Ravi's courses:   " + ravi.courses());
        try {
            java.enroll(new Student("Kiran"));
        } catch (IllegalStateException e) {
            System.out.println("Multiplicity 0..2 enforced: " + e.getMessage());
        }
        java.drop(ravi);
        System.out.println("After drop, JAVA101: " + java.students() + ", Ravi: " + ravi.courses());

        System.out.println("\n== Multiplicity 2 and 0..1 ==");
        FlightInstance f = new FlightInstance("AI-202", List.of(new Pilot("Capt. Rao"), new Pilot("F/O Das")));
        System.out.println(f.flightNo() + " pilots: " + f.pilots());
        try {
            new FlightInstance("AI-303", List.of(new Pilot("Solo")));
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
        ParkingSpot spot = new ParkingSpot(7);
        System.out.println("Spot 7 vehicle: " + spot.vehicle().map(Vehicle::plate).orElse("<empty>"));
        spot.park(new Vehicle("KA-01-1234"));
        System.out.println("Spot 7 vehicle: " + spot.vehicle().map(Vehicle::plate).orElse("<empty>"));
    }
}
