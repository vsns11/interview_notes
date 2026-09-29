package ch17.v18;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/** JEP 400 (Java 18): UTF-8 is the default charset on every OS, regardless of locale. */
public class Utf8Default {
    public static void main(String[] args) throws Exception {
        System.out.println("Charset.defaultCharset(): " + Charset.defaultCharset());
        System.out.println("file.encoding           : " + System.getProperty("file.encoding"));
        System.out.println("native.encoding (OS)    : " + System.getProperty("native.encoding"));

        Path tmp = Files.createTempFile("utf8", ".txt");
        Files.writeString(tmp, "café ☕");                           // no charset argument needed
        try (var reader = new java.io.FileReader(tmp.toFile())) {    // FileReader also uses UTF-8 now
            char[] buf = new char[16];
            int n = reader.read(buf);
            System.out.println("read back               : " + new String(buf, 0, n) + " (" + Files.size(tmp) + " bytes)");
        }
        Files.delete(tmp);
    }
}
