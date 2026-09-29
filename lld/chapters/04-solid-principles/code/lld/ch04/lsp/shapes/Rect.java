package lld.ch04.lsp.shapes;

/** Immutable: "resizing" returns a new object, so no setter can break an invariant. */
public record Rect(int width, int height) implements Shape {
    public Rect withWidth(int w) {
        return new Rect(w, height);
    }

    @Override
    public int area() {
        return width * height;
    }
}
