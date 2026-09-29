package ch08.basics;

public class FirstException {

    static int average(int[] values) {
        int sum = 0;
        for (int v : values) sum += v;
        return sum / values.length;            // length 0 -> ArithmeticException
    }

    public static void main(String[] args) {
        System.out.println("average {4, 8} = " + average(new int[]{4, 8}));
        try {
            System.out.println("before the call");
            int avg = average(new int[0]);     // throws; the rest of the try block is skipped
            System.out.println("never printed " + avg);
        } catch (ArithmeticException e) {
            System.out.println("caught " + e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("program continues normally");

        // Throwing your own exception to reject a bad argument
        try {
            setAge(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("caught IllegalArgumentException: " + e.getMessage());
        }
    }

    static void setAge(int age) {
        if (age < 0) throw new IllegalArgumentException("age must be >= 0, was " + age);
        System.out.println("age set to " + age);
    }
}
