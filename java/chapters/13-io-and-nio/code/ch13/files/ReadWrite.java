package ch13.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

/** The Files one-liners for whole files, plus lazy line streaming and open options. */
public class ReadWrite {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java1315_rw");
        try {
            Path f = dir.resolve("todo.txt");

            Files.writeString(f, "buy milk\n");                          // CREATE, TRUNCATE_EXISTING, WRITE
            Files.writeString(f, "call bob\n", StandardOpenOption.APPEND);
            Files.write(f, List.of("fix bug #42", "ship v2"), StandardOpenOption.APPEND);
            System.out.print("readString:\n" + Files.readString(f));

            List<String> lines = Files.readAllLines(f);                    // whole file in memory
            System.out.println("readAllLines size = " + lines.size() + " -> " + lines);

            try (Stream<String> s = Files.lines(f)) {                      // lazy; MUST be closed
                long withB = s.filter(l -> l.contains("b")).count();
                System.out.println("lines containing 'b' = " + withB);
            }

            byte[] raw = Files.readAllBytes(f);
            System.out.println("bytes = " + raw.length);

            try {
                Files.writeString(f, "x", StandardOpenOption.CREATE_NEW);  // fails if file exists
            } catch (FileAlreadyExistsException e) {
                System.out.println("CREATE_NEW on existing -> " + e.getClass().getSimpleName());
            }

            Path csv = dir.resolve("scores.csv");
            try (BufferedWriter w = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {
                w.write("name,score"); w.newLine();
                w.write("ana,91");     w.newLine();
                w.write("raj,78");     w.newLine();
            }                                                              // close() flushes
            int total = 0;
            try (BufferedReader r = Files.newBufferedReader(csv)) {
                r.readLine();                                              // skip header
                String line;
                while ((line = r.readLine()) != null) {                    // null = end of file
                    total += Integer.parseInt(line.split(",")[1]);
                }
            }
            System.out.println("total score = " + total);
        } finally {
            FileOps.deleteRecursively(dir);
        }
    }
}
