package lld.ch03.aggregation;

/** A part that has its own life: it is created outside any airline and can move between airlines. */
public final class Aircraft {
    private final String registration;
    private final String model;

    public Aircraft(String registration, String model) {
        this.registration = registration;
        this.model = model;
    }

    public String registration() { return registration; }

    @Override public String toString() { return registration + " (" + model + ")"; }
}
