package ch03.patterns;

/** Pattern matching for switch (Java 21): type patterns, guards, null, exhaustiveness with sealed types. */
public class SwitchPatterns {
    sealed interface Shape permits Circle, Square, Rectangle {}
    record Circle(double radius) implements Shape {}
    record Square(double side) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}

    static double area(Shape shape) {
        return switch (shape) {                          // exhaustive: every permitted subtype covered
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Square s -> s.side() * s.side();
            case Rectangle r -> r.width() * r.height();
        };
    }

    static String classify(Object o) {
        return switch (o) {
            case null -> "null!";                        // without this case, switch throws NPE
            case Integer i when i < 0 -> "negative int " + i;   // guard
            case Integer i -> "int " + i;
            case String s when s.isBlank() -> "blank string";
            case String s -> "string of " + s.length();
            case int[] arr -> "int array of " + arr.length;
            case Shape sh -> "shape with area " + String.format("%.2f", area(sh));
            default -> "other: " + o.getClass().getSimpleName();
        };
    }

    public static void main(String[] args) {
        Object[] samples = {-7, 42, "  ", "hello", new int[3], new Circle(1), new Rectangle(2, 3), 2.5, null};
        for (Object o : samples) System.out.println(classify(o));
    }
}
