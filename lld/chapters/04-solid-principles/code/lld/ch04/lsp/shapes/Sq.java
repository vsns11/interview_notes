package lld.ch04.lsp.shapes;

public record Sq(int side) implements Shape {
    @Override
    public int area() {
        return side * side;
    }
}
