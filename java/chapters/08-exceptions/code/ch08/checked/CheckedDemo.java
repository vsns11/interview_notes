package ch08.checked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class CheckedDemo {

    // "Declare": this method does not handle IOException, it passes it to the caller
    static String readConfig(Path path) throws IOException {
        return Files.readString(path);
    }

    // "Handle": this method catches and decides what to do
    static String readConfigOrDefault(Path path) {
        try {
            return readConfig(path);
        } catch (NoSuchFileException e) {            // more specific first
            System.out.println("no such file: " + e.getFile() + " -> using defaults");
            return "mode=default";
        } catch (IOException e) {                    // any other I/O problem
            System.out.println("I/O failure: " + e);
            return "mode=default";
        }
    }

    public static void main(String[] args) throws IOException {
        Path tmp = Files.createTempFile("ch08-", ".properties");
        Files.writeString(tmp, "mode=fast");
        System.out.println("existing file: " + readConfigOrDefault(tmp));
        Files.delete(tmp);
        System.out.println("deleted file:  " + readConfigOrDefault(Path.of("missing-config.properties")));

        // Unchecked: the compiler does not force you to handle NumberFormatException
        String[] inputs = {"42", "forty-two"};
        for (String in : inputs) {
            try {
                System.out.println("parsed " + Integer.parseInt(in));
            } catch (NumberFormatException e) {
                System.out.println("bad number '" + in + "': " + e.getMessage());
            }
        }
    }
}
