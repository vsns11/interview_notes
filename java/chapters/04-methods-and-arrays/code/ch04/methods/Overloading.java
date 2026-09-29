package ch04.methods;

public class Overloading {

    static String describe(int x)      { return "int"; }
    static String describe(long x)     { return "long"; }
    static String describe(Integer x)  { return "Integer"; }
    static String describe(Object x)   { return "Object"; }
    static String describe(int... xs)  { return "int..."; }

    static String pick(long x)    { return "long (widening)"; }
    static String pick(Integer x) { return "Integer (boxing)"; }

    static String box(Object o)   { return "Object"; }
    static String box(double d)   { return "double"; }

    // Overloads differ by parameter list; the return type alone cannot distinguish them.
    static int    sum(int a, int b)           { return a + b; }
    static int    sum(int a, int b, int c)    { return a + b + c; }
    static double sum(double a, double b)     { return a + b; }

    public static void main(String[] args) {
        byte b = 1; short s = 2; char c = 'A';
        System.out.println("describe(5)        -> " + describe(5));         // exact match
        System.out.println("describe(5L)       -> " + describe(5L));
        System.out.println("describe(b)        -> " + describe(b));         // byte widened to int
        System.out.println("describe(c)        -> " + describe(c));         // char widened to int
        System.out.println("describe(\"hi\")     -> " + describe("hi"));    // String -> Object
        System.out.println("describe()         -> " + describe());          // only varargs fits
        System.out.println("describe(1, 2)     -> " + describe(1, 2));
        System.out.println("pick(5)            -> " + pick(5));             // widening beats boxing
        System.out.println("box(5)             -> " + box(5));              // int -> double widening beats boxing to Integer
        System.out.println("sum(1,2) sum(1,2,3) sum(1.5,2) -> "
                + sum(1, 2) + " " + sum(1, 2, 3) + " " + sum(1.5, 2));
    }
}
