package ch03.patterns;

/** Record patterns (Java 21) and unnamed patterns _ (Java 22). */
public class RecordPatterns {
    record Point(int x, int y) {}
    sealed interface Expr permits Num, Add, Mul, Neg {}
    record Num(int value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Neg(Expr operand) implements Expr {}

    static int eval(Expr e) {
        return switch (e) {
            case Num(int v) -> v;
            case Add(Expr l, Expr r) -> eval(l) + eval(r);
            case Mul(Num(var v), _) when v == 0 -> 0;          // nested pattern + unnamed _ + guard
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
            case Neg(var inner) -> -eval(inner);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num(int v) -> String.valueOf(v);
            case Add(var l, var r) -> "(" + show(l) + " + " + show(r) + ")";
            case Mul(var l, var r) -> show(l) + " * " + show(r);
            case Neg(var x) -> "-" + show(x);
        };
    }

    public static void main(String[] args) {
        Object o = new Point(3, 4);
        if (o instanceof Point(int x, int y)) {              // deconstruct in one step
            System.out.println("x=" + x + " y=" + y + " distance=" + Math.sqrt(x * x + y * y));
        }
        if (o instanceof Point(var x, _)) System.out.println("only care about x: " + x);

        Expr expr = new Add(new Num(2), new Mul(new Num(3), new Neg(new Num(4))));
        System.out.println(show(expr) + " = " + eval(expr));
        Expr zero = new Mul(new Num(0), new Add(new Num(1), new Num(2)));
        System.out.println(show(zero) + " = " + eval(zero));
    }
}
