package lld.ch10.interpreter;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Double> context = Map.of("price", 250.0, "qty", 3.0, "discount", 50.0, "a", 7.0, "b", 3.0);
        System.out.println("context: " + new java.util.TreeMap<>(context));

        List<String> inputs = List.of(
            "price * qty - discount",
            "2 + 3 * 4",
            "(2 + 3) * 4",
            "(a + b) * (a - b)",
            "-a + 10 / 4",
            "price / (qty - 3)",
            "price * tax",
            "2 + * 3");

        for (String text : inputs) {
            try {
                Expression ast = Parser.parse(text);          // build the tree once...
                double value = ast.interpret(context);        // ...interpret it (could be many times)
                System.out.printf("  %-24s AST %-32s = %s%n", text, ast.show(), value);
            } catch (IllegalArgumentException | ArithmeticException e) {
                System.out.printf("  %-24s ERROR: %s%n", text, e.getMessage());
            }
        }

        System.out.println("\nSame tree, different contexts (parse once, evaluate many):");
        Expression total = Parser.parse("price * qty - discount");
        for (double q : new double[] {1, 2, 4}) {
            System.out.println("  qty=" + (int) q + " -> " + total.interpret(Map.of("price", 250.0, "qty", q, "discount", 50.0)));
        }
    }
}
