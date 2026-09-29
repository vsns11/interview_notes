// A second source file that the source launcher finds and compiles on demand (Java 22+).
public class Greeter {
    static String greet(String who) {
        return "Hello from " + who + ", compiled in memory";
    }
}
