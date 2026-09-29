package lld.ch11.state;

/**
 * State pattern the idiomatic-Java way: an enum whose constants override behaviour.
 * The whole machine is readable in one file.
 */
public enum TrafficLight {
    RED(30) {
        TrafficLight next() { return GREEN; }
        boolean pedestriansMayCross() { return true; }
    },
    GREEN(25) {
        TrafficLight next() { return YELLOW; }
    },
    YELLOW(5) {
        TrafficLight next() { return RED; }
    };

    private final int seconds;

    TrafficLight(int seconds) { this.seconds = seconds; }

    int seconds() { return seconds; }
    abstract TrafficLight next();
    boolean pedestriansMayCross() { return false; }    // default for most states
}
