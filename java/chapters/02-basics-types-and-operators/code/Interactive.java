package ch02.io;

// tools_run: skip  (waits for keyboard input)
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Four ways to read from the keyboard. Pick one per program: each reader buffers System.in,
 * so mixing them loses input. Run: java -cp out ch02.io.Interactive scanner|io|reader|console
 */
public class Interactive {
    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "scanner";
        switch (mode) {
            case "scanner" -> {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Your name: ");
                String name = scanner.nextLine();
                System.out.print("Your age: ");
                int age = scanner.nextInt();
                System.out.println("Hi " + name + ", next year you'll be " + (age + 1));
            }
            case "io" -> {                                   // java.lang.IO, Java 25
                String city = IO.readln("City: ");
                IO.println("You live in " + city);
            }
            case "reader" -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Number: ");
                int n = Integer.parseInt(reader.readLine().trim());
                System.out.println("Squared: " + n * n);
            }
            case "console" -> {
                Console console = System.console();          // null if not an interactive terminal
                if (console == null) {
                    System.out.println("No interactive console (IDE or redirected input).");
                    return;
                }
                char[] secret = console.readPassword("Password (not echoed): ");
                if (secret == null) return;                   // end of input (Ctrl+D)
                System.out.println("Password length: " + secret.length);
                Arrays.fill(secret, ' ');                     // wipe it from memory
            }
            default -> System.out.println("unknown mode " + mode);
        }
    }
}
