package ch05.encapsulation;

// Encapsulated: state is private, every change goes through methods that protect the invariant
// "MIN <= target <= MAX".
public class Thermostat {
    public static final double MIN = 5.0, MAX = 30.0;
    private double targetCelsius = 20.0;
    private int changes;

    public double getTargetCelsius() { return targetCelsius; }        // getter (accessor)

    public void setTargetCelsius(double value) {                      // setter (mutator) with validation
        if (Double.isNaN(value) || value < MIN || value > MAX)
            throw new IllegalArgumentException("target must be in [" + MIN + ", " + MAX + "]: " + value);
        targetCelsius = value;
        changes++;
    }

    public void raise(double delta) { setTargetCelsius(Math.min(MAX, targetCelsius + delta)); } // behaviour, not just data

    public double getTargetFahrenheit() { return targetCelsius * 9 / 5 + 32; } // derived value, no field needed

    public int getChanges() { return changes; }                        // read-only property: no setter
}
