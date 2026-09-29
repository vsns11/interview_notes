package lld.ch02.polymorphism;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        System.out.println(calc.add(2, 3));            // exact match int,int
        System.out.println(calc.add(2L, 3));           // widening to long,long
        System.out.println(calc.add(2.5, 3));          // widening to double,double
        System.out.println(calc.add(1, 2, 3));         // varargs, chosen last

        Shape s = new Circle(1.0);                     // static type Shape, runtime type Circle
        System.out.println("s.area() -> " + String.format("%.2f", s.area()) + "  (runtime override: dynamic dispatch)");
        System.out.println("calc.greet(s) -> " + calc.greet(s) + "  (overload picked at compile time from static type)");
        System.out.println("s.kind -> " + s.kind + "  (fields use the static type)");
        System.out.println("((Circle) s).kind -> " + ((Circle) s).kind);
        System.out.println("Shape.describe() -> " + Shape.describe() + ", Circle.describe() -> " + Circle.describe());
        System.out.println("s.copy() class -> " + s.copy().getClass().getSimpleName());
        System.out.println(s.who());

        List<Shape> shapes = List.of(new Shape(), new Circle(2), new Circle(0.5));
        double total = 0;
        for (Shape sh : shapes) total += sh.area();     // one call site, many behaviours
        System.out.println("total area = " + String.format("%.2f", total));

        if (s instanceof Circle c) {                    // pattern matching instanceof (Java 16+)
            System.out.println("pattern-matched a circle: " + c.who());
        }
    }
}
