package lld.ch16.airline;

enum FlightStatus { SCHEDULED, DELAYED, DEPARTED, LANDED, CANCELLED }
enum ReservationStatus { CONFIRMED, CANCELLED }
enum SeatClass { ECONOMY, ECONOMY_PLUS, PREFERRED_ECONOMY, BUSINESS, FIRST_CLASS }
enum SeatType { REGULAR, ACCESSIBLE, EMERGENCY_EXIT, EXTRA_LEG_ROOM }
enum CrewRole { PILOT, CO_PILOT, CABIN_CREW }

record Airport(String code, String city) {}

/** The PHYSICAL seat bolted to an aircraft. It knows nothing about bookings. */
record Seat(String number, SeatType type, SeatClass seatClass) {}

/** A name on a ticket. Not an account holder: a customer can book for other people. */
record Passenger(String name, String passportRef) {}

record Customer(String id, String name, String email) {}

record CrewMember(String id, String name, CrewRole role) {}

/** Staff actors. Admin-only operations take an Admin parameter, so the compiler enforces "only the admin". */
record Admin(String id, String name) {}
record FrontDeskOfficer(String id, String name) {}
