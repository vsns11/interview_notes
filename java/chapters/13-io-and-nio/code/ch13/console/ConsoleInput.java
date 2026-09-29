package ch13.console;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Reading "keyboard" input. We fake the keyboard with an in-memory stream so the demo is repeatable. */
public class ConsoleInput {
    public static void main(String[] args) throws IOException {
        String typed = "Ada\n36\n3 4 5\n";

        // 1) Scanner: token-based parsing, convenient for small programs and exercises
        Scanner sc = new Scanner(new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8)));
        String name = sc.nextLine();
        int age = sc.nextInt();
        int sum = 0;
        while (sc.hasNextInt()) sum += sc.nextInt();
        System.out.println("Scanner: name=" + name + " age=" + age + " sum=" + sum);

        // 2) BufferedReader: line-based, much faster for big inputs (competitive programming, log parsing)
        BufferedReader br = new BufferedReader(new StringReader(typed));
        String first = br.readLine();
        int parsedAge = Integer.parseInt(br.readLine().trim());
        int total = 0;
        for (String tok : br.readLine().split("\\s+")) total += Integer.parseInt(tok);
        System.out.println("BufferedReader: name=" + first + " age=" + parsedAge + " sum=" + total);

        // 3) The nextInt-then-nextLine trap
        Scanner trap = new Scanner("42\nhello\n");
        int n = trap.nextInt();
        String rest = trap.nextLine();            // reads the leftover "" after 42, not "hello"
        System.out.println("trap: n=" + n + " nextLine=[" + rest + "] then [" + trap.nextLine() + "]");

        // 4) Standard streams
        System.out.println("stdout and stderr are separate streams (redirect with > and 2>)");
        System.err.println("this line went to stderr");

        // 5) Console is null when there is no interactive terminal (IDE run, pipes, this runner)
        Console c = System.console();
        System.out.println("System.console() available? " + (c != null && c.isTerminal()));  // isTerminal: Java 22

        // 6) java.lang.IO (Java 25, JEP 512): println/print/readln for beginner programs
        IO.println("IO.println works without System.out");
    }
}
