package lld.ch02.polymorphism;

public class Shape {
    public String kind = "shape";                                // fields are NOT polymorphic

    public double area() { return 0; }

    public Shape copy() { return new Shape(); }

    public static String describe() { return "static Shape.describe"; }   // static: hidden, not overridden

    public String who() { return "I am a " + kind + " with area " + String.format("%.2f", area()); }
}
