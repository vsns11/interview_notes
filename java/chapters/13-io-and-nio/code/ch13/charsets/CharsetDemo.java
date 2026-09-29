package ch13.charsets;

import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

/** Text is chars; files are bytes. A charset is the dictionary between them. */
public class CharsetDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("default charset = " + Charset.defaultCharset() + " (UTF-8 by default since Java 18, JEP 400)");

        String s = "café €";
        HexFormat hex = HexFormat.ofDelimiter(" ");                          // Java 17
        for (Charset cs : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16BE,
                                        StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII}) {
            byte[] b = s.getBytes(cs);
            System.out.printf("%-10s %2d bytes  %s%n", cs, b.length, hex.formatHex(b));
        }

        // Mojibake: bytes written as UTF-8, decoded as ISO-8859-1
        byte[] utf8 = "café".getBytes(StandardCharsets.UTF_8);
        System.out.println("decoded wrong  -> " + new String(utf8, StandardCharsets.ISO_8859_1));
        System.out.println("decoded right  -> " + new String(utf8, StandardCharsets.UTF_8));

        // Files.readString is strict: malformed input throws instead of guessing
        Path dir = Files.createTempDirectory("java1315_cs");
        Path latin = dir.resolve("latin1.txt");
        try {
            Files.writeString(latin, "café", StandardCharsets.ISO_8859_1);   // é = single byte 0xE9
            try {
                Files.readString(latin);                                       // assumes UTF-8
            } catch (MalformedInputException e) {
                System.out.println("readString(latin1 file) -> " + e.getClass().getSimpleName());
            }
            System.out.println("with right charset -> " + Files.readString(latin, StandardCharsets.ISO_8859_1));
        } finally {
            Files.deleteIfExists(latin); Files.delete(dir);
        }

        // chars vs code points: emoji need two chars (a surrogate pair)
        String e = "hi👋";
        System.out.println("length()=" + e.length() + " codePoints=" + e.codePoints().count()
                + " utf8 bytes=" + e.getBytes(StandardCharsets.UTF_8).length);
    }
}
