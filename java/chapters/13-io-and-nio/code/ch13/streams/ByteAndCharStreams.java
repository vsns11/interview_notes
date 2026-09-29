package ch13.streams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Byte streams, character streams, bridges between them, and decorators stacked on each other. */
public class ByteAndCharStreams {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java1315_streams");
        try {
            // 1) Byte stream: copy a file byte-by-byte-ish with a buffer array (the classic loop)
            Path src = Files.writeString(dir.resolve("src.bin"), "Hello, bytes!");
            Path dst = dir.resolve("dst.bin");
            try (InputStream in = new FileInputStream(src.toFile());
                 OutputStream out = new FileOutputStream(dst.toFile())) {
                byte[] buf = new byte[4];                // tiny buffer to show the loop
                int n, reads = 0;
                while ((n = in.read(buf)) != -1) {       // -1 = end of stream
                    out.write(buf, 0, n);                // write only the n bytes actually read
                    reads++;
                }
                System.out.println("copied with " + reads + " read() calls -> " + Files.readString(dst));
            }

            // 2) transferTo (Java 9) does the loop for you
            try (InputStream in = Files.newInputStream(src);
                 OutputStream out = Files.newOutputStream(dir.resolve("copy2.bin"))) {
                System.out.println("transferTo copied " + in.transferTo(out) + " bytes");
            }

            // 3) Character streams: Reader/Writer work in chars, need a charset at the byte boundary
            Path text = dir.resolve("greet.txt");
            try (Writer w = new OutputStreamWriter(new FileOutputStream(text.toFile()), StandardCharsets.UTF_8)) {
                w.write("naïve café ☕");                 // 12 chars
            }
            System.out.println("chars written = 12, bytes on disk = " + Files.size(text));
            try (Reader r = new InputStreamReader(new FileInputStream(text.toFile()), StandardCharsets.UTF_8)) {
                int c, count = 0;
                while ((c = r.read()) != -1) count++;
                System.out.println("chars read back = " + count);
            }

            // 4) Decorators: PrintWriter -> BufferedWriter -> OutputStreamWriter -> GZIPOutputStream -> FileOutputStream
            Path gz = dir.resolve("log.txt.gz");
            try (PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new GZIPOutputStream(new FileOutputStream(gz.toFile())), StandardCharsets.UTF_8)))) {
                for (int i = 1; i <= 1000; i++) pw.printf("line %d: all work and no play%n", i);
            }                                                 // closing the outermost closes the whole chain
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(Files.newInputStream(gz)), StandardCharsets.UTF_8))) {
                System.out.println("gzip file " + Files.size(gz) + " bytes holds " + br.lines().count() + " lines");
            }

            // 5) In-memory streams: handy for tests
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8)) {
                ps.print("captured");
            }
            System.out.println("ByteArrayOutputStream -> " + bos.toString(StandardCharsets.UTF_8));
            StringWriter sw = new StringWriter();
            new StringReader("abc").transferTo(sw);           // Reader.transferTo: Java 10
            System.out.println("StringReader -> StringWriter: " + sw);
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }
}
