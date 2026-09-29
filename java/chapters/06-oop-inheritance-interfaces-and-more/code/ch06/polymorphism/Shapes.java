package ch06.polymorphism;

import java.util.List;

public class Shapes {

    static class Shape {
        double area() { return 0; }
        String name() { return getClass().getSimpleName(); }
    }
    static class Circle extends Shape {
        final double r;
        Circle(double r) { this.r = r; }
        @Override double area() { return Math.PI * r * r; }
    }
    static class Rect extends Shape {
        final double w, h;
        Rect(double w, double h) { this.w = w; this.h = h; }
        @Override double area() { return w * h; }
    }
    static class Square extends Rect {
        Square(double side) { super(side, side); }
        double side() { return w; }
    }

    public static void main(String[] args) {
        List<Shape> shapes = List.of(new Circle(1), new Rect(2, 3), new Square(2));   // upcasting: implicit
        double total = 0;
        for (Shape s : shapes) {                           // one loop, many behaviours
            System.out.printf("%-6s area = %.2f%n", s.name(), s.area());
            total += s.area();
        }
        System.out.printf("total = %.2f%n", total);

        Shape s = shapes.get(2);
        // s.side();                -> compile error: the static type Shape has no side()
        if (s instanceof Square sq) {                      // pattern matching for instanceof (Java 16)
            System.out.println("pattern variable: side = " + sq.side());
        }
        Rect r = (Rect) s;                                 // explicit downcast: checked at runtime
        System.out.println("downcast to Rect ok: w=" + r.w);
        try {
            Circle c = (Circle) s;                         // compiles (Circle IS-A Shape) but fails at runtime
        } catch (ClassCastException e) {
            System.out.println("ClassCastException: " + e.getMessage().replaceAll(" \\(.*", ""));
        }
    }
}
