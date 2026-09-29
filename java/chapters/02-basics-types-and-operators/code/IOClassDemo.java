package ch02.io;

/** java.lang.IO (Java 25) works in ordinary classes too. */
public class IOClassDemo {
    public static void main(String[] args) {
        IO.print("IO.print, ");
        IO.println("IO.println");
        IO.println(42);
        IO.println();
        IO.println("done");
    }
}
