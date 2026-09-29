package lld.ch08.prototype;

import java.util.ArrayList;
import java.util.List;

/** Prototype interface: lets code copy an object whose concrete class it does not know. */
interface Shape {
    Shape copy();
    String describe();
}

final class Circle implements Shape {
    private final int radius;
    private final String color;
    Circle(int radius, String color) { this.radius = radius; this.color = color; }
    private Circle(Circle other) { this(other.radius, other.color); }            // copy constructor
    @Override public Circle copy() { return new Circle(this); }                  // covariant return
    @Override public String describe() { return color + " circle r=" + radius; }
}

final class Rectangle implements Shape {
    private final int w, h;
    private final String color;
    Rectangle(int w, int h, String color) { this.w = w; this.h = h; this.color = color; }
    @Override public Rectangle copy() { return new Rectangle(w, h, color); }
    @Override public String describe() { return color + " rectangle " + w + "x" + h; }
}

/** A composite prototype: deep copy must copy the children too. */
final class Group implements Shape {
    private final List<Shape> children = new ArrayList<>();
    Group add(Shape s) { children.add(s); return this; }
    @Override public Group copy() {
        Group g = new Group();
        for (Shape s : children) g.add(s.copy());       // recursive deep copy
        return g;
    }
    @Override public String describe() { return "group" + children.stream().map(Shape::describe).toList(); }
    int size() { return children.size(); }
}
