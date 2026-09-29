package ch04.args;

import java.util.Arrays;

// Run:  java ch04.args.CommandLineArgs 3 7 --verbose
public class CommandLineArgs {
    public static void main(String[] args) {
        if (args.length == 0) {                      // the runner passes no arguments
            args = new String[] {"3", "7", "x", "--verbose"};
            System.out.println("(no args given, using demo args)");
        }
        System.out.println("args = " + Arrays.toString(args) + ", count = " + args.length);
        boolean verbose = false;
        int sum = 0;
        for (String a : args) {
            if (a.equals("--verbose")) { verbose = true; continue; }
            try {
                sum += Integer.parseInt(a);          // every argument arrives as a String
            } catch (NumberFormatException e) {
                System.out.println("skipping non-number: " + a);
            }
        }
        System.out.println("sum = " + sum + (verbose ? " (verbose mode)" : ""));
    }
}
