package ch04.methods;

public class MethodBasics {

    // modifiers  return-type  name   (parameter list)
    static        double       area(double width, double height) {
        return width * height;                 // 'return' hands a value back to the caller
    }

    static void greet(String name) {           // void = returns nothing
        if (name == null || name.isBlank()) {
            System.out.println("Hello, stranger");
            return;                            // early exit from a void method
        }
        System.out.println("Hello, " + name);
    }

    static boolean isEven(int n) { return n % 2 == 0; }

    static int max(int a, int b) { return a >= b ? a : b; }

    // An instance method needs an object to run on
    private int calls;
    int countCall() { return ++calls; }

    public static void main(String[] args) {
        double a = area(3, 4.5);               // int arguments are widened to double
        System.out.println("area = " + a);
        greet("Ada");
        greet("  ");
        System.out.println("isEven(7) = " + isEven(7) + ", max(3, 9) = " + max(3, 9));
        area(1, 1);                            // result ignored: legal, the value is discarded

        MethodBasics counter = new MethodBasics();
        counter.countCall();
        System.out.println("calls = " + counter.countCall());
    }
}
