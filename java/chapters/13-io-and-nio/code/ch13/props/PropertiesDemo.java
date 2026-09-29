package ch13.props;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeSet;

/** key=value configuration files. */
public class PropertiesDemo {
    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("java1315_props");
        Path file = dir.resolve("app.properties");
        try {
            Files.writeString(file, """
                    # database settings
                    db.url = jdbc:postgresql://localhost:5432/shop
                    db.pool.size=10
                    ! bang also starts a comment
                    greeting=Hello, \\
                             world
                    city=Zürich
                    """);

            Properties p = new Properties();
            try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(r);                                   // use a Reader to control the charset
            }
            System.out.println("keys        = " + new TreeSet<>(p.stringPropertyNames()));
            System.out.println("db.url      = " + p.getProperty("db.url"));
            int pool = Integer.parseInt(p.getProperty("db.pool.size"));
            System.out.println("pool+1      = " + (pool + 1) + "   (values are always Strings)");
            System.out.println("greeting    = " + p.getProperty("greeting"));
            System.out.println("city        = " + p.getProperty("city"));
            System.out.println("timeout     = " + p.getProperty("timeout", "30") + "   (default used)");

            p.setProperty("timeout", "45");
            Path out = dir.resolve("saved.properties");
            try (Writer w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                p.store(w, "saved by demo");                 // writes a comment + date line first
            }
            long lines = Files.readAllLines(out).stream().filter(l -> !l.startsWith("#")).count();
            System.out.println("saved " + lines + " key lines");

            System.out.println("system property java.version starts with 2? "
                    + System.getProperty("java.version").startsWith("2"));
        } finally {
            Files.deleteIfExists(file); Files.deleteIfExists(dir.resolve("saved.properties")); Files.delete(dir);
        }
    }
}
