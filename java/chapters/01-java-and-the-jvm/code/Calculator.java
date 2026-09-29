package ch01.bytecode;

/** A tiny class used to look at the bytecode javac produces (see javap -c). */
public class Calculator {
    static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        int total = add(2, 3);
        System.out.println("2 + 3 = " + total);
    }
}
