package ch05.encapsulation;

public class EncapsulationDemo {

    static class OpenThermostat { public double targetCelsius; }   // no encapsulation

    public static void main(String[] args) {
        OpenThermostat open = new OpenThermostat();
        open.targetCelsius = -273;                                  // nothing stops nonsense
        System.out.println("open thermostat: " + open.targetCelsius);

        Thermostat t = new Thermostat();
        t.setTargetCelsius(22.5);
        t.raise(10);                                                // clamped to MAX
        System.out.println("target=" + t.getTargetCelsius() + "C = " + t.getTargetFahrenheit()
                + "F, changes=" + t.getChanges());
        try {
            t.setTargetCelsius(-273);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
