// A compact source file (Java 25, JEP 512): no class declaration, instance main, java.base auto-imported.
void main() {
    List<String> langs = List.of("Java", "Kotlin", "Scala");   // java.util.List without an import
    IO.println("Hello from a compact source file!");
    IO.println("JVM languages: " + langs);
}
