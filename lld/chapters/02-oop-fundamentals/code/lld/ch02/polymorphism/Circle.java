package lld.ch02.polymorphism;

public class Circle extends Shape {
    public String kind = "circle";                               // HIDES Shape.kind, does not override it
    private final double r;

    public Circle(double r) {
        super();                                                 // implicit anyway; parent constructed first
        this.r = r;
    }

    @Override public double area() { return Math.PI * r * r; }  // OVERRIDE: same signature

    @Override public Circle copy() { return new Circle(r); }    // covariant return type is allowed

    public static String describe() { return "static Circle.describe"; }

    @Override public String who() { return super.who() + " (radius " + r + ")"; }  // super.method() reuses parent code
}
