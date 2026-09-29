package ch13.streams;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/** One OS write per byte vs. one per 8 KiB. Timings are machine-dependent; the ratio is the lesson. */
public class BufferingDemo {
    static final int N = 200_000;

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java1315_buf");
        Path f1 = dir.resolve("unbuffered.bin"), f2 = dir.resolve("buffered.bin");
        try {
            long t0 = System.nanoTime();
            try (OutputStream out = new FileOutputStream(f1.toFile())) {
                for (int i = 0; i < N; i++) out.write('x');          // each call = a system call
            }
            long t1 = System.nanoTime();
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(f2.toFile()))) {
                for (int i = 0; i < N; i++) out.write('x');          // copied into an 8 KiB array
            }
            long t2 = System.nanoTime();
            System.out.println("same size: " + (Files.size(f1) == Files.size(f2)) + " (" + Files.size(f2) + " bytes)");
            System.out.printf("unbuffered: %d ms   [machine-dependent]%n", (t1 - t0) / 1_000_000);
            System.out.printf("buffered:   %d ms   [machine-dependent]%n", (t2 - t1) / 1_000_000);
            System.out.println("buffered faster? " + ((t2 - t1) < (t1 - t0)));

            // Forgetting to flush/close a buffered writer loses data
            Path lost = dir.resolve("lost.txt");
            BufferedWriter w = new BufferedWriter(new FileWriter(lost.toFile()));
            w.write("this sits in the buffer");
            System.out.println("before flush: " + Files.size(lost) + " bytes on disk");
            w.flush();
            System.out.println("after flush:  " + Files.size(lost) + " bytes on disk");
            w.close();
        } finally {
            Files.deleteIfExists(f1); Files.deleteIfExists(f2);
            Files.deleteIfExists(dir.resolve("lost.txt")); Files.delete(dir);
        }
    }
}
