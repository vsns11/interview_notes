package lld.ch02.polymorphism;

/** OVERLOADING: same name, different parameter lists. Chosen by the COMPILER from static types. */
public class Calculator {
    public String add(int a, int b)       { return "add(int,int) = " + (a + b); }
    public String add(long a, long b)     { return "add(long,long) = " + (a + b); }
    public String add(double a, double b) { return "add(double,double) = " + (a + b); }
    public String add(int... xs)          { int s = 0; for (int x : xs) s += x; return "add(int...) = " + s; }

    public String greet(Shape s)  { return "greet(Shape)"; }
    public String greet(Circle c) { return "greet(Circle)"; }
}
