package lld.ch15.hotel;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

enum BookingStatus { REQUESTED, PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, ABANDONED }

/** The actors. Each service method takes the actor type allowed to call it, so roles are enforced by types. */
sealed interface Person permits Guest, Receptionist, Server, Housekeeper, Manager { String name(); }
record Guest(String id, String name, String email) implements Person {}
record Receptionist(String id, String name) implements Person {}
record Server(String id, String name) implements Person {}
record Housekeeper(String id, String name) implements Person {}
record Manager(String id, String name) implements Person {}

interface Notifier { void send(String guestId, String message); }

/**
 * One stay in one room. Owns its invoice (composition); only points at the room (association).
 * Records the guest by id, which answers "who booked this room?" and "which rooms did this guest book?".
 */
final class RoomBooking {
    static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    static final Duration FULL_REFUND_NOTICE = Duration.ofHours(24);

    private final String reservationNumber;
    private final String guestId;
    private final Room room;
    private final LocalDate checkIn, checkOut;
    private final Invoice invoice;
    private BookingStatus status = BookingStatus.CONFIRMED;
    private BigDecimal deposit = BigDecimal.ZERO;

    RoomBooking(String reservationNumber, String guestId, Room room, LocalDate checkIn, LocalDate checkOut) {
        this.reservationNumber = reservationNumber; this.guestId = guestId; this.room = room;
        this.checkIn = checkIn; this.checkOut = checkOut; this.invoice = new Invoice("INV-" + reservationNumber);
    }
    String reservationNumber() { return reservationNumber; }
    String guestId()           { return guestId; }
    Room room()                { return room; }
    LocalDate checkIn()        { return checkIn; }
    LocalDate checkOut()       { return checkOut; }
    Invoice invoice()          { return invoice; }
    long nights()              { return checkIn.until(checkOut).getDays(); }
    synchronized BookingStatus status()        { return status; }
    synchronized void setStatus(BookingStatus s) { status = s; }
    synchronized BigDecimal deposit()             { return deposit; }
    synchronized void setDeposit(BigDecimal d)    { deposit = d; }

    /** The refund rule lives in exactly ONE method: full refund if cancelled more than 24h before check-in. */
    BigDecimal refundDue(LocalDateTime now) {
        Duration notice = Duration.between(now, checkIn.atTime(CHECK_IN_TIME));
        // compareTo, not toHours(): toHours() truncates, so 24h30m would wrongly count as "24, not more than 24".
        return notice.compareTo(FULL_REFUND_NOTICE) > 0 ? deposit() : BigDecimal.ZERO;
    }
}

/** The front desk: every flow of a stay, from booking to checkout. */
final class FrontDesk {
    private final HotelLocation location;
    private final Clock clock;
    private final Notifier notifier;
    private final Map<String, RoomBooking> bookings = new ConcurrentHashMap<>();
    private final List<Person> staff = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(), keySeq = new AtomicInteger();

    FrontDesk(HotelLocation location, Clock clock, Notifier notifier) {
        this.location = location; this.clock = clock; this.notifier = notifier;
    }

    private LocalDateTime now() { return LocalDateTime.now(clock); }

    void addRoom(Manager by, Room r) { location.addRoom(r); }

    /** The manager adds new workers (receptionists, servers, housekeepers). */
    void hire(Manager by, Person worker) {
        if (worker instanceof Guest) throw new IllegalArgumentException("a guest is not a worker");
        staff.add(worker);
    }
    List<String> staff() { return staff.stream().map(Person::name).toList(); }

    /** Guest or receptionist books: same call, two actors. The room's monitor makes it race-free. */
    RoomBooking book(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, Payment deposit) {
        if (!checkIn.isBefore(checkOut)) throw new IllegalArgumentException("check-out must be after check-in");
        if (!room.tryReserve(checkIn, checkOut))
            throw new IllegalStateException("room " + room.number() + " is not free " + checkIn + ".." + checkOut);
        RoomBooking b = new RoomBooking("R" + seq.incrementAndGet(), guest.id(), room, checkIn, checkOut);
        if (!deposit.initiateTransaction()) {                  // payment failed: give the nights back
            room.release(checkIn, checkOut);
            throw new IllegalStateException("deposit " + deposit.status());
        }
        b.setDeposit(deposit.amount());
        bookings.put(b.reservationNumber(), b);
        notifier.send(guest.id(), b.reservationNumber() + " confirmed: room " + room.number() + " " + checkIn + ".." + checkOut);
        return b;
    }

    RoomBooking bookFor(Receptionist by, Guest guest, Room room, LocalDate in, LocalDate out, Payment deposit) {
        return book(guest, room, in, out, deposit);
    }

    RoomKey checkIn(Receptionist by, String reservationNumber) {
        RoomBooking b = get(reservationNumber);
        synchronized (b) {
            if (b.status() != BookingStatus.CONFIRMED) throw new IllegalStateException(reservationNumber + " is " + b.status());
            if (now().toLocalDate().isBefore(b.checkIn())) throw new IllegalStateException("too early to check in");
            b.setStatus(BookingStatus.CHECKED_IN);
            b.room().setStatus(RoomStatus.OCCUPIED);
            return b.room().issueKey("K" + keySeq.incrementAndGet(), now());
        }
    }

    void addCharge(Server by, String reservationNumber, RoomCharge charge) {
        RoomBooking b = get(reservationNumber);
        synchronized (b) {                     // same monitor as checkOut: no charge can slip in after the bill is totalled
            if (b.status() != BookingStatus.CHECKED_IN) throw new IllegalStateException("guest not in house");
            charge.addInvoiceItem(b.invoice());
        }
    }

    /** Adds the room nights, credits the deposit, takes payment for the balance, kills the keys. */
    Invoice checkOut(Receptionist by, String reservationNumber, Function<BigDecimal, Payment> payWith) {
        RoomBooking b = get(reservationNumber);
        synchronized (b) {
            if (b.status() != BookingStatus.CHECKED_IN) throw new IllegalStateException(reservationNumber + " is " + b.status());
            Invoice inv = b.invoice();
            BigDecimal price = b.room().pricePerNight();
            List<InvoiceItem> finalLines = new ArrayList<>(List.of(
                    new InvoiceItem(b.nights() + " nights x " + Money.fmt(price), price.multiply(BigDecimal.valueOf(b.nights()))),
                    new InvoiceItem("Deposit paid", b.deposit().negate())));
            BigDecimal balance = finalLines.stream().map(InvoiceItem::amount).reduce(inv.total(), BigDecimal::add);
            Payment p = payWith.apply(balance);
            // Write the final lines only once payment succeeds, so a declined payment can be retried
            // without the nights and deposit lines being added to the invoice twice.
            if (!p.initiateTransaction()) throw new IllegalStateException("payment " + p.status() + ", guest still checked in");
            finalLines.forEach(inv::addItem);
            int killed = b.room().deactivateGuestKeys();
            b.setStatus(BookingStatus.CHECKED_OUT);
            b.room().setStatus(RoomStatus.BEING_SERVICED);
            notifier.send(b.guestId(), "checked out, paid " + Money.fmt(inv.total()) + " by " + p + " [" + p.status()
                    + "], " + killed + " key(s) deactivated");
            return inv;
        }
    }

    void logHousekeeping(Housekeeper by, Room room, String description, int minutes) {
        room.addHousekeeping(new RoomHouseKeeping(now(), by.name(), description, minutes));
        room.setStatus(RoomStatus.AVAILABLE);
    }

    BigDecimal cancel(Person by, String reservationNumber) {
        RoomBooking b = get(reservationNumber);
        synchronized (b) {
            if (b.status() != BookingStatus.CONFIRMED) throw new IllegalStateException(reservationNumber + " is " + b.status());
            BigDecimal refund = b.refundDue(now());
            b.room().release(b.checkIn(), b.checkOut());
            b.setStatus(BookingStatus.CANCELLED);
            notifier.send(b.guestId(), reservationNumber + " cancelled by " + by.name() + ", refund " + Money.fmt(refund));
            return refund;
        }
    }

    /** System job: remind guests whose check-in or check-out is within the next 24 hours. */
    void sendReminders() {
        LocalDateTime now = now(), soon = now.plusHours(24);
        bookings.values().stream().sorted((x, y) -> x.reservationNumber().compareTo(y.reservationNumber())).forEach(b -> {
            LocalDateTime in = b.checkIn().atTime(RoomBooking.CHECK_IN_TIME), out = b.checkOut().atTime(11, 0);
            if (b.status() == BookingStatus.CONFIRMED && in.isAfter(now) && !in.isAfter(soon))
                notifier.send(b.guestId(), "reminder: check-in for " + b.reservationNumber() + " at " + in);
            if (b.status() == BookingStatus.CHECKED_IN && out.isAfter(now) && !out.isAfter(soon))
                notifier.send(b.guestId(), "reminder: check-out for " + b.reservationNumber() + " at " + out);
        });
    }

    List<String> roomsBookedBy(String guestId) {
        return bookings.values().stream().filter(b -> b.guestId().equals(guestId) && b.status() != BookingStatus.CANCELLED)
                .map(b -> b.room().number()).sorted().toList();
    }

    List<String> whoBooked(String roomNumber) {
        return bookings.values().stream().filter(b -> b.room().number().equals(roomNumber) && b.status() != BookingStatus.CANCELLED)
                .map(b -> b.guestId() + "@" + b.checkIn()).sorted().toList();
    }

    RoomBooking get(String n) {
        RoomBooking b = bookings.get(n);
        if (b == null) throw new IllegalArgumentException("no booking " + n);
        return b;
    }
}
