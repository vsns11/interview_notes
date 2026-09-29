package lld.ch20.carpool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Machine-coding variant (Flipkart-style "ride sharing / carpool"): users OFFER seats in their own
 * vehicle on a route; other users SELECT a ride with a selection strategy.
 */
public class Main {

    record User(String name, String gender, int age) {}

    record Vehicle(String owner, String model, String plate) {}

    enum RideStatus { ACTIVE, ENDED }

    static final class Ride {
        final String id;
        final User offeredBy;
        final Vehicle vehicle;
        final String origin, destination;
        private int availableSeats;
        private RideStatus status = RideStatus.ACTIVE;
        private final List<User> riders = new ArrayList<>();

        Ride(String id, User offeredBy, Vehicle vehicle, String origin, String destination, int seats) {
            this.id = id;
            this.offeredBy = offeredBy;
            this.vehicle = vehicle;
            this.origin = origin;
            this.destination = destination;
            this.availableSeats = seats;
        }

        synchronized int availableSeats() { return availableSeats; }

        /** Atomic check-and-book, so two users cannot take the last seat. */
        synchronized boolean book(User u, int seats) {
            if (status != RideStatus.ACTIVE || availableSeats < seats) return false;
            availableSeats -= seats;
            riders.add(u);
            return true;
        }

        @Override
        public String toString() {
            return id + " " + offeredBy.name() + " " + vehicle.model() + " " + origin + "->" + destination
                    + " seats=" + availableSeats;
        }
    }

    /** Strategy: pick one of the matching rides. */
    interface RideSelectionStrategy {
        Optional<Ride> select(List<Ride> candidates);
    }

    static final class MostVacant implements RideSelectionStrategy {
        public Optional<Ride> select(List<Ride> c) { return c.stream().max(Comparator.comparingInt(Ride::availableSeats)); }
    }

    record PreferredVehicle(String model) implements RideSelectionStrategy {
        public Optional<Ride> select(List<Ride> c) {
            return c.stream().filter(r -> r.vehicle.model().equalsIgnoreCase(model)).findFirst();
        }
    }

    static final class RideService {
        private final Map<String, User> users = new LinkedHashMap<>();
        private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();   // plate -> vehicle
        private final Map<String, Ride> rides = new LinkedHashMap<>();
        private final Map<String, int[]> stats = new LinkedHashMap<>();        // name -> {taken, offered}
        private int nextId = 0;

        void addUser(String name, String gender, int age) {
            users.put(name, new User(name, gender, age));
            stats.put(name, new int[2]);
        }

        void addVehicle(String owner, String model, String plate) { vehicles.put(plate, new Vehicle(owner, model, plate)); }

        /** Fails if this vehicle already has an ACTIVE offered ride. */
        synchronized Optional<Ride> offerRide(String name, String origin, int seats, String plate, String destination) {
            Vehicle v = vehicles.get(plate);
            if (v == null || !v.owner().equals(name)) return Optional.empty();
            boolean busy = rides.values().stream()
                    .anyMatch(r -> r.vehicle.plate().equals(plate) && r.status == RideStatus.ACTIVE);
            if (busy) return Optional.empty();
            Ride r = new Ride("2" + (char) ('a' + nextId++), users.get(name), v, origin, destination, seats);
            rides.put(r.id, r);
            return Optional.of(r);
        }

        synchronized Optional<Ride> selectRide(String name, String origin, String destination, int seats,
                                               RideSelectionStrategy strategy) {
            if (seats < 1 || seats > 2) throw new IllegalArgumentException("a user may request 1 or 2 seats");
            List<Ride> matching = rides.values().stream()
                    .filter(r -> r.status == RideStatus.ACTIVE)
                    .filter(r -> r.origin.equals(origin) && r.destination.equals(destination))
                    .filter(r -> !r.offeredBy.name().equals(name))
                    .filter(r -> r.availableSeats() >= seats)
                    .toList();
            Optional<Ride> chosen = strategy.select(matching);
            chosen.ifPresent(r -> r.book(users.get(name), seats));
            return chosen;
        }

        synchronized void endRide(String rideId) {
            Ride r = rides.get(rideId);
            if (r == null || r.status == RideStatus.ENDED) return;
            r.status = RideStatus.ENDED;
            stats.get(r.offeredBy.name())[1]++;
            for (User u : r.riders) stats.get(u.name())[0]++;
        }

        void printRideStats() {
            stats.forEach((n, s) -> System.out.println("  " + n + ": " + s[0] + " Taken, " + s[1] + " Offered"));
        }
    }

    public static void main(String[] args) {
        RideService s = new RideService();
        s.addUser("Rohan", "M", 36);    s.addVehicle("Rohan", "Swift", "KA-01-12345");
        s.addUser("Shashank", "M", 29); s.addVehicle("Shashank", "Baleno", "TS-05-62395");
        s.addUser("Nandini", "F", 29);
        s.addUser("Shipra", "F", 27);   s.addVehicle("Shipra", "Polo", "KA-05-41491");
                                        s.addVehicle("Shipra", "Activa", "KA-12-12332");
        s.addUser("Gaurav", "M", 29);
        s.addUser("Rahul", "M", 35);    s.addVehicle("Rahul", "XUV", "KA-05-1234");

        System.out.println("== offer_ride");
        print(s.offerRide("Rohan", "Hyderabad", 1, "KA-01-12345", "Bangalore"));
        print(s.offerRide("Shipra", "Bangalore", 1, "KA-12-12332", "Mysore"));
        print(s.offerRide("Shipra", "Bangalore", 2, "KA-05-41491", "Mysore"));
        print(s.offerRide("Shashank", "Hyderabad", 2, "TS-05-62395", "Bangalore"));
        print(s.offerRide("Rahul", "Hyderabad", 5, "KA-05-1234", "Bangalore"));
        print(s.offerRide("Rohan", "Bangalore", 1, "KA-01-12345", "Pune")); // same vehicle still active

        System.out.println("== select_ride");
        print(s.selectRide("Nandini", "Bangalore", "Mysore", 1, new MostVacant()));
        print(s.selectRide("Gaurav", "Bangalore", "Mysore", 1, new PreferredVehicle("Activa")));
        print(s.selectRide("Shashank", "Mumbai", "Bangalore", 1, new MostVacant()));
        print(s.selectRide("Rohan", "Hyderabad", "Bangalore", 1, new PreferredVehicle("Baleno")));
        print(s.selectRide("Shashank", "Hyderabad", "Bangalore", 1, new PreferredVehicle("Polo")));

        System.out.println("== end_ride 2a..2d, then print_ride_stats");
        for (String id : List.of("2a", "2b", "2c", "2d")) s.endRide(id);
        s.printRideStats();
    }

    private static void print(Optional<Ride> r) {
        System.out.println("  " + r.map(Ride::toString).orElse("No rides found / request failed"));
    }
}
