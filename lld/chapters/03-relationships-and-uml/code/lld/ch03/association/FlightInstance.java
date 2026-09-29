package lld.ch03.association;

import java.util.List;

/** Multiplicity "exactly 2": the constructor enforces the number written on the diagram. */
public final class FlightInstance {
    private final String flightNo;
    private final List<Pilot> pilots;

    public FlightInstance(String flightNo, List<Pilot> pilots) {
        if (pilots.size() != 2) {
            throw new IllegalArgumentException("a flight needs exactly 2 pilots, got " + pilots.size());
        }
        this.flightNo = flightNo;
        this.pilots = List.copyOf(pilots);
    }

    public List<Pilot> pilots() { return pilots; }
    public String flightNo() { return flightNo; }
}
