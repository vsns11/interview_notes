// A compact source file (Java 25, JEP 512): no class declaration, no public static.
// Run directly with:  java HelloCompact.java
void main() {
    String name = "compact world";
    IO.println("Hello, " + name + "!");
    IO.println("Lists are auto-imported: " + List.of(1, 2, 3));
}
