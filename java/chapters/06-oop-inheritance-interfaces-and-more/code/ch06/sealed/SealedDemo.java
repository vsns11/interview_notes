package ch06.sealed;

public class SealedDemo {

    // Only the listed types may implement Shape (Java 17). Each must be final, sealed, or non-sealed.
    sealed interface Shape permits Circle, Rectangle, Polygon {}

    record Circle(double radius) implements Shape {}                 // records are implicitly final
    record Rectangle(double w, double h) implements Shape {}
    static non-sealed class Polygon implements Shape {               // open again: anyone may extend Polygon
        final int sides;
        Polygon(int sides) { this.sides = sides; }
    }
    static final class Triangle extends Polygon { Triangle() { super(3); } }

    static String describe(Shape s) {
        // Exhaustive: the compiler knows every permitted subtype, so no 'default' branch is needed.
        return switch (s) {
            case Circle c                               -> "circle r=" + c.radius();
            case Rectangle(double w, double h) when w == h -> "square " + w;      // record pattern + guard
            case Rectangle(double w, double h)          -> "rectangle " + w + "x" + h;
            case Polygon p                              -> p.sides + "-sided polygon";
        };
    }

    public static void main(String[] args) {
        Shape[] shapes = { new Circle(1.5), new Rectangle(2, 2), new Rectangle(2, 5), new Triangle() };
        for (Shape s : shapes) System.out.println(describe(s));
        System.out.println("Shape is sealed? " + Shape.class.isSealed()
                + ", permitted: " + Shape.class.getPermittedSubclasses().length);
    }
}
