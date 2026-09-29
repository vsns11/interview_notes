package lld.ch14.parkinglot;

/** Panels hold no rules. They talk to the lot and print what happened. */
class EntrancePanel {
    private final String id;
    private final ParkingLot lot;

    EntrancePanel(String id, ParkingLot lot) { this.id = id; this.lot = lot; }

    String issueTicket(Vehicle v) {
        return lot.admit(v)
                .map(t -> "[%s] %s -> ticket %s, spot %s".formatted(id, v.plate(), t.id(), t.spot()))
                .orElse("[%s] %s -> SORRY, LOT FULL for %s".formatted(id, v.plate(), v.type()));
    }
}

class ExitPanel {
    private final String id;
    private final ParkingLot lot;

    ExitPanel(String id, ParkingLot lot) { this.id = id; this.lot = lot; }

    String scanPayAndExit(String ticketId, PaymentMethod method) {
        long charged = lot.pay(ticketId, method);
        boolean open = lot.exit(ticketId);
        String paid = charged == 0 ? "already paid" : "paid " + Money.fmt(charged) + " by " + method.describe();
        return "[%s] %s: %s, barrier %s".formatted(id, ticketId, paid, open ? "OPEN" : "CLOSED");
    }
}

/** On each floor: lets the customer pay early so the exit is free. */
class CustomerInfoPortal {
    private final ParkingLot lot;

    CustomerInfoPortal(ParkingLot lot) { this.lot = lot; }

    String pay(String ticketId, PaymentMethod method) {
        long due = lot.amountDue(ticketId);
        long charged = lot.pay(ticketId, method);
        return "[portal] %s due %s, charged %s".formatted(ticketId, Money.fmt(due), Money.fmt(charged));
    }
}
