package lld.ch16.restaurant;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class Reservation {
    private final String id;
    private final Customer customer;
    private int partySize;
    private final LocalDateTime time;
    private final Table table;
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    private boolean reminded;

    Reservation(String id, Customer customer, int partySize, LocalDateTime time, Table table) {
        this.id = id; this.customer = customer; this.partySize = partySize; this.time = time; this.table = table;
    }
    String id() { return id; }
    Customer customer() { return customer; }
    synchronized int partySize() { return partySize; }

    /** The party grew or shrank. Refused if the reserved table cannot seat the new number. */
    synchronized void updatePeopleCount(int n) {
        if (status != ReservationStatus.CONFIRMED) throw new IllegalStateException(id + " is " + status);
        if (n < 1 || n > table.capacity())
            throw new IllegalArgumentException("table " + table.id() + " seats " + table.capacity() + ", not " + n);
        partySize = n;
    }
    LocalDateTime time() { return time; }
    Table table() { return table; }
    synchronized ReservationStatus status() { return status; }

    synchronized void cancel() {
        if (status != ReservationStatus.CONFIRMED) throw new IllegalStateException(id + " is " + status);
        status = ReservationStatus.CANCELLED;
        table.release(time);
    }

    synchronized void checkIn() {
        if (status != ReservationStatus.CONFIRMED) throw new IllegalStateException(id + " is " + status);
        status = ReservationStatus.CHECKED_IN;
        table.setStatus(TableStatus.OCCUPIED);
    }

    synchronized boolean markReminded() { boolean first = !reminded; reminded = true; return first; }
}

interface Notifier { void send(Customer to, String message); }

final class SmsNotifier implements Notifier {
    public void send(Customer to, String message) { System.out.println("    [sms " + to.phone() + "] " + message); }
}

/** Searching belongs here, not on Table: one table only knows about itself. */
final class ReservationService {
    private final List<Table> tables;
    private final Notifier notifier;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    ReservationService(List<Table> tables, Notifier notifier) { this.tables = List.copyOf(tables); this.notifier = notifier; }

    /** Best fit first: the smallest table that seats the party, so big tables stay free for big parties. */
    List<Table> search(LocalDateTime time, int partySize) {
        return tables.stream().filter(t -> t.capacity() >= partySize && t.isFreeAt(time))
                .sorted(Comparator.comparingInt(Table::capacity)).toList();
    }

    Optional<Reservation> reserve(Customer c, int partySize, LocalDateTime time) {
        for (Table t : search(time, partySize)) {
            if (t.tryHold(time)) {     // another receptionist may have taken it since search(): try the next one
                var r = new Reservation("RES-" + seq.incrementAndGet(), c, partySize, time, t);
                reservations.put(r.id(), r);
                notifier.send(c, "Table " + t.id() + " booked for " + partySize + " at " + time);
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    Reservation get(String id) {
        var r = reservations.get(id);
        if (r == null) throw new IllegalArgumentException("no reservation " + id);
        return r;
    }

    void cancel(String id) {
        var r = get(id);
        r.cancel();
        notifier.send(r.customer(), "Reservation " + id + " cancelled");
    }

    Table checkIn(String id) {
        var r = get(id);
        r.checkIn();
        return r.table();
    }

    List<Table> freeForWalkIn(int partySize) {
        return tables.stream().filter(t -> t.status() == TableStatus.FREE && t.capacity() >= partySize)
                .sorted(Comparator.comparingInt(Table::capacity)).toList();
    }

    /** Run by a scheduler every few minutes: remind reservations starting within the next hour, once. */
    void sendReminders(LocalDateTime now) {
        reservations.values().stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED)
                .filter(r -> !r.time().isBefore(now) && r.time().isBefore(now.plusHours(1)))
                .filter(Reservation::markReminded)
                .forEach(r -> notifier.send(r.customer(), "Reminder: your table " + r.table().id() + " at " + r.time().toLocalTime()));
    }
}
