package ch17.v21;

/** JEPs 440 + 441 (Java 21): record patterns and pattern matching for switch. Details: Chapter 3. */
public class PatternsDemo {
    sealed interface Expr permits Num, Add, Mul, Neg {}
    record Num(int value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Neg(Expr e) implements Expr {}

    static int eval(Expr e) {
        return switch (e) {                                          // exhaustive: no default needed (sealed)
            case Num(int v) -> v;
            case Add(Expr l, Expr r) -> eval(l) + eval(r);
            case Mul(Num(int a), Num(int b)) when a == 0 || b == 0 -> 0;   // nested pattern + guard
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
            case Neg(Neg(Expr inner)) -> eval(inner);                // double negation
            case Neg(Expr inner) -> -eval(inner);
        };
    }

    static String describe(Object o) {
        return switch (o) {
            case null -> "null!";                                    // switch can handle null explicitly
            case Integer i when i > 100 -> "big int " + i;
            case Integer i -> "int " + i;
            case String s -> "string of length " + s.length();
            default -> "something else: " + o.getClass().getSimpleName();
        };
    }

    public static void main(String[] args) {
        Expr e = new Add(new Num(2), new Mul(new Num(3), new Neg(new Neg(new Num(4)))));
        System.out.println(e + " = " + eval(e));
        for (Object o : new Object[] {7, 700, "hey", 2.5, null}) System.out.println(describe(o));
    }
}
