package ch02.variables;

/** Declaring, initialising, reassigning, final, scope. */
public class Variables {
    static int counter;          // field: gets a default value (0) automatically

    public static void main(String[] args) {
        int age;                 // declaration
        age = 30;                // initialisation (assignment)
        int year = 2025, month = 9;   // declare + initialise several of the same type
        age = age + 1;           // reassignment
        final double TAX_RATE = 0.18; // final: can be assigned exactly once
        // TAX_RATE = 0.2;       // compile error: cannot assign a value to final variable TAX_RATE

        System.out.println("age=" + age + " year=" + year + " month=" + month);
        System.out.println("TAX_RATE=" + TAX_RATE + " counter (field default)=" + counter);

        {                                   // an inner block
            int inner = 5;                  // exists only inside these braces
            System.out.println("inner=" + inner + ", age visible here too: " + age);
        }
        // System.out.println(inner);      // compile error: cannot find symbol

        int count;
        if (age > 18) count = 1; else count = 0;   // definitely assigned on every path
        System.out.println("count=" + count);
    }
}
