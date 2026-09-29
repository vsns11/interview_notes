package lld.ch02.composition;

/** A car IS NOT an engine; it HAS an engine. The part can be swapped at run time. */
public class Car {
    private final String model;
    private Engine engine;

    public Car(String model, Engine engine) { this.model = model; this.engine = engine; }

    public void replaceEngine(Engine e) { this.engine = e; }

    public String drive() { return model + ": " + engine.start(); }
}
