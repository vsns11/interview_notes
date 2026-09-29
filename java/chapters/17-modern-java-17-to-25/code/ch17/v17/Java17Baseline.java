package ch17.v17;

/** The language you get "for free" when you move from Java 11 to 17 (features finalised in 14-17). */
public class Java17Baseline {
    sealed interface Shape permits Circle, Square {}                 // sealed classes: Java 17
    record Circle(double r) implements Shape {}                      // records: Java 16
    record Square(double side) implements Shape {}

    static double area(Shape s) {
        if (s instanceof Circle c) return Math.PI * c.r() * c.r();   // pattern matching for instanceof: Java 16
        if (s instanceof Square q) return q.side() * q.side();
        throw new AssertionError();
    }

    static String kind(int day) {
        return switch (day) {                                         // switch expression: Java 14
            case 6, 7 -> "weekend";
            case 1, 2, 3, 4, 5 -> "weekday";
            default -> throw new IllegalArgumentException("day " + day);
        };
    }

    record Customer(String name, Address address) {}
    record Address(String city) {}

    public static void main(String[] args) {
        String json = """
                {"shape": "circle", "r": 1.5}
                """;                                                   // text block: Java 15
        System.out.print("text block : " + json);
        System.out.printf("areas      : %.2f %.2f%n", area(new Circle(1.5)), area(new Square(2)));
        System.out.println("switch     : " + kind(6) + ", " + kind(3));
        System.out.println("record     : " + new Circle(1.5));

        Customer c = new Customer("ana", null);
        try {
            System.out.println(c.address().city().length());
        } catch (NullPointerException e) {                            // helpful NPE messages: Java 14
            System.out.println("NPE says   : " + e.getMessage());
        }
    }
}
