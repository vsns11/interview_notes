package lld.ch11.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A subject that supports both notification styles, to compare PUSH and PULL. */
public final class WeatherStation {
    /** PUSH: the subject sends the data with the notification. */
    public record Reading(double tempC, double humidity, double pressureHpa) {}

    private final List<Consumer<Reading>> pushObservers = new ArrayList<>();
    private final List<Consumer<WeatherStation>> pullObservers = new ArrayList<>();
    private Reading current = new Reading(0, 0, 0);

    public void subscribePush(Consumer<Reading> o) { pushObservers.add(o); }
    public void subscribePull(Consumer<WeatherStation> o) { pullObservers.add(o); }

    // PULL: observers get only "I changed" plus a reference, then ask for what they need.
    public double temperature() { return current.tempC(); }
    public double humidity() { return current.humidity(); }

    public void measure(Reading r) {
        current = r;                                             // change state FIRST...
        pushObservers.forEach(o -> o.accept(r));                 // ...then notify
        pullObservers.forEach(o -> o.accept(this));
    }
}
