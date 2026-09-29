package lld.ch06.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Clean functions: small, one thing, one level of abstraction, few args, no flags, no hidden side effects, CQS, exceptions. */
@SuppressWarnings("serial") // the nested exception classes are never serialized
public class Main {

    // ---------------- BEFORE ----------------
    static class Before {
        static final Map<String, Object> SESSION = new HashMap<>();

        /**
         * Returns total in rupees, or -1 for bad input.
         * Does validation, pricing, surcharges, discounts, logging and session updates -- all in one place.
         */
        static int book(String seatType, int count, boolean weekend, List<String> log) {   // flag arg + output arg
            if (count <= 0) return -1;                                                   // error code
            int price;
            switch (seatType) {                                                          // switch on a type code
                case "SILVER": price = 150; break;
                case "GOLD": price = 250; break;
                case "RECLINER": price = 450; break;
                default: return -1;
            }
            if (weekend) price = price + price * 20 / 100;
            int total = price * count;
            if (count >= 4) total = total - total * 10 / 100;
            log.add("booked " + count + " " + seatType);                                 // mutates the caller's list
            SESSION.put("lastTotal", total);                                             // hidden side effect
            return total;
        }
    }

    // ---------------- AFTER ----------------
    static class After {
        /** The type-code switch is replaced by data on the enum; parsing happens once, at the boundary. */
        enum SeatType {
            SILVER(150), GOLD(250), RECLINER(450);
            final int basePrice;
            SeatType(int basePrice) { this.basePrice = basePrice; }
            static SeatType parse(String raw) {
                try { return valueOf(raw); }
                catch (IllegalArgumentException e) { throw new InvalidBookingException("unknown seat type '" + raw + "'"); }
            }
        }
        enum DayType {                         // replaces the boolean flag: callers write DayType.WEEKEND, not 'true'
            WEEKDAY(0), WEEKEND(20);
            final int surchargePercent;
            DayType(int p) { surchargePercent = p; }
        }
        /** Argument object: three related arguments become one concept. */
        record BookingRequest(SeatType seat, int seats, DayType day) {}

        static final class InvalidBookingException extends RuntimeException {
            InvalidBookingException(String message) { super(message); }
        }

        /** Stepdown rule: read top to bottom, each function one level more detailed than the one above. */
        static final class TicketPricer {
            private static final int GROUP_SIZE = 4;
            private static final int GROUP_DISCOUNT_PERCENT = 10;

            int totalFor(BookingRequest request) {                        // level 1: the story
                requireAtLeastOneSeat(request);
                int total = pricePerSeat(request) * request.seats();
                return applyGroupDiscount(total, request.seats());
            }

            private void requireAtLeastOneSeat(BookingRequest r) {        // level 2: the details
                if (r.seats() <= 0) throw new InvalidBookingException("seats must be positive, was " + r.seats());
            }
            private int pricePerSeat(BookingRequest r) {
                return addPercent(r.seat().basePrice, r.day().surchargePercent);
            }
            private int applyGroupDiscount(int total, int seats) {
                return seats >= GROUP_SIZE ? addPercent(total, -GROUP_DISCOUNT_PERCENT) : total;
            }
            private static int addPercent(int amount, int percent) {      // level 3: arithmetic
                return amount + amount * percent / 100;
            }
        }

        /** Command-Query Separation: book() changes state and returns nothing; history() answers and changes nothing. */
        static final class BookingService {
            private final TicketPricer pricer = new TicketPricer();
            private final List<String> history = new ArrayList<>();

            void book(BookingRequest request) {
                int total = pricer.totalFor(request);
                history.add(request.seats() + " x " + request.seat() + " on " + request.day() + " = Rs " + total);
            }
            List<String> history() { return List.copyOf(history); }
        }

        /** Error handling is "one thing": this function only translates failures for the UI. */
        static String bookFromForm(BookingService service, String seat, int seats, DayType day) {
            try {
                service.book(new BookingRequest(SeatType.parse(seat), seats, day));
                return "OK";
            } catch (InvalidBookingException e) {
                return "Sorry: " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        List<String> log = new ArrayList<>();
        int result = Before.book("GOLD", 4, true, log);           // what does 'true' mean here?
        System.out.println("total=" + result + " log=" + log + " session=" + Before.SESSION);
        int bad = Before.book("PLATINUM", 2, false, log);
        System.out.println("bad seat returns " + bad + " -- caller must remember to check for -1");

        System.out.println("== AFTER ==");
        var service = new After.BookingService();
        System.out.println(After.bookFromForm(service, "GOLD", 4, After.DayType.WEEKEND));
        System.out.println(After.bookFromForm(service, "SILVER", 2, After.DayType.WEEKDAY));
        System.out.println(After.bookFromForm(service, "PLATINUM", 2, After.DayType.WEEKDAY));
        System.out.println(After.bookFromForm(service, "GOLD", 0, After.DayType.WEEKDAY));
        service.history().forEach(h -> System.out.println("  history: " + h));
    }
}
