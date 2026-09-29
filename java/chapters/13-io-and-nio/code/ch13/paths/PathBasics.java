package ch13.paths;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Path is just a name: none of these calls touch the disk. */
public class PathBasics {
    public static void main(String[] args) {
        Path p = Path.of("reports", "2025", "q3", "sales.csv");   // Path.of: Java 11
        System.out.println("path          = " + p);
        System.out.println("fileName      = " + p.getFileName());
        System.out.println("parent        = " + p.getParent());
        System.out.println("root          = " + p.getRoot());       // null: relative path
        System.out.println("nameCount     = " + p.getNameCount());
        System.out.println("name(1)       = " + p.getName(1));
        System.out.println("subpath(1,3)  = " + p.subpath(1, 3));
        System.out.println("isAbsolute    = " + p.isAbsolute());

        Path base = Path.of("/srv/app");
        System.out.println("resolve       = " + base.resolve("logs/app.log"));
        System.out.println("resolve abs   = " + base.resolve("/etc/hosts"));      // absolute arg wins
        System.out.println("resolveSibling= " + base.resolve("config.yml").resolveSibling("secrets.yml"));

        Path messy = Path.of("/srv/app/./logs/../data/./users.db");
        System.out.println("normalize     = " + messy.normalize());

        Path from = Path.of("/srv/app/logs");
        Path to = Path.of("/srv/app/data/users.db");
        System.out.println("relativize    = " + from.relativize(to));
        System.out.println("round trip    = " + from.resolve(from.relativize(to)).normalize());

        System.out.println("startsWith    = " + to.startsWith("/srv/app") + " / string-wise 'app' = " + to.startsWith("/srv/ap"));
        System.out.println("endsWith      = " + to.endsWith("data/users.db"));

        // Interop with the legacy java.io.File class
        File legacy = p.toFile();
        Path back = legacy.toPath();
        System.out.println("File <-> Path = " + legacy.getPath() + " -> " + back.equals(p));
        System.out.println("Paths.get     = " + Paths.get("a", "b").equals(Path.of("a", "b")));
    }
}
