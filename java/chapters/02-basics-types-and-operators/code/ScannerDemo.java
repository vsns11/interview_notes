package ch02.io;

import java.util.Scanner;

/** Scanner basics. Reads from a String here so the program is deterministic; use new Scanner(System.in) for the keyboard. */
public class ScannerDemo {
    public static void main(String[] args) {
        String fakeKeyboard = """
                Ada Lovelace
                36
                1.5 2.5 3.0
                done
                """;
        Scanner in = new Scanner(fakeKeyboard);
        String fullName = in.nextLine();          // whole line
        int age = in.nextInt();                   // one token -> int
        in.nextLine();                            // consume the rest of the "36" line!
        double sum = 0;
        while (in.hasNextDouble()) sum += in.nextDouble();
        String word = in.next();                  // next token
        System.out.println("name=" + fullName + ", age=" + age + ", sum=" + sum + ", word=" + word);

        Scanner trap = new Scanner("42\nAlan Turing\n");
        int n = trap.nextInt();
        String afterInt = trap.nextLine();        // returns the EMPTY rest of the first line
        System.out.println("n=" + n + ", nextLine after nextInt=[" + afterInt + "], then=[" + trap.nextLine() + "]");

        Scanner bad = new Scanner("forty-two");
        if (!bad.hasNextInt()) System.out.println("not an int: " + bad.next());
    }
}
