package lld.ch10.mediator;

import java.util.ArrayDeque;
import java.util.Queue;

/** Air traffic control: aircraft talk to the tower, never to each other. */
final class ControlTower {
    private Flight onRunway;                           // null = runway free
    private final Queue<Flight> holding = new ArrayDeque<>();

    void requestLanding(Flight f) {
        if (onRunway == null) {
            clear(f);
        } else {
            holding.add(f);
            System.out.println("  [tower] " + f.callsign() + ": hold, runway busy with " + onRunway.callsign());
        }
    }

    void runwayVacated(Flight f) {
        System.out.println("  [tower] " + f.callsign() + " vacated the runway");
        onRunway = null;
        Flight next = holding.poll();
        if (next != null) clear(next);
    }

    private void clear(Flight f) {
        onRunway = f;
        System.out.println("  [tower] " + f.callsign() + ": cleared to land");
    }
}
