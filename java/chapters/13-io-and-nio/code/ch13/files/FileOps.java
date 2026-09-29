package ch13.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Create / copy / move / delete / inspect / list files - all inside a temp directory that is removed at the end. */
public class FileOps {
    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("java1315_fileops");
        try {
            Path docs = Files.createDirectories(root.resolve("docs/2025"));   // makes parents too
            Path a = Files.writeString(docs.resolve("a.txt"), "alpha\n");
            System.out.println("exists a.txt?        " + Files.exists(a));
            System.out.println("isDirectory docs?    " + Files.isDirectory(docs));
            System.out.println("size a.txt           " + Files.size(a) + " bytes");

            try {
                Files.createDirectory(docs);                                  // already there
            } catch (FileAlreadyExistsException e) {
                System.out.println("createDirectory again -> " + e.getClass().getSimpleName());
            }

            Path b = Files.copy(a, docs.resolve("b.txt"));
            try {
                Files.copy(a, b);                                             // target exists
            } catch (FileAlreadyExistsException e) {
                System.out.println("copy onto existing   -> " + e.getClass().getSimpleName());
            }
            Files.copy(a, b, StandardCopyOption.REPLACE_EXISTING);           // now allowed

            Path archive = Files.createDirectory(root.resolve("archive"));
            Path moved = Files.move(b, archive.resolve("b-old.txt"));        // also a rename
            System.out.println("after move b exists? " + Files.exists(b) + ", moved exists? " + Files.exists(moved));

            System.out.println("same content a/moved " + (Files.mismatch(a, moved) == -1)); // Java 12

            BasicFileAttributes attrs = Files.readAttributes(a, BasicFileAttributes.class);
            System.out.println("attrs: regular=" + attrs.isRegularFile() + " dir=" + attrs.isDirectory()
                    + " size=" + attrs.size() + " created<=modified " +
                    (attrs.creationTime().compareTo(attrs.lastModifiedTime()) <= 0));

            Files.writeString(docs.resolve("notes.md"), "# notes\n");
            Files.writeString(root.resolve("readme.md"), "# readme\n");

            System.out.println("-- Files.list(root) (one level, sorted):");
            try (Stream<Path> s = Files.list(root)) {
                s.map(p -> root.relativize(p).toString()).sorted().forEach(n -> System.out.println("   " + n));
            }
            System.out.println("-- Files.walk(root) (recursive, sorted):");
            try (Stream<Path> s = Files.walk(root)) {
                s.map(p -> root.relativize(p).toString()).sorted().forEach(n -> System.out.println("   '" + n + "'"));
            }
            System.out.println("-- Files.find *.md:");
            try (Stream<Path> s = Files.find(root, 10, (p, at) -> at.isRegularFile() && p.toString().endsWith(".md"))) {
                s.map(p -> root.relativize(p).toString()).sorted().forEach(n -> System.out.println("   " + n));
            }

            try {
                Files.delete(docs);                                           // not empty
            } catch (DirectoryNotEmptyException e) {
                System.out.println("delete non-empty dir -> " + e.getClass().getSimpleName());
            }
            System.out.println("deleteIfExists(missing) = " + Files.deleteIfExists(root.resolve("nope.txt")));
            try {
                Files.readString(root.resolve("nope.txt"));
            } catch (NoSuchFileException e) {
                System.out.println("read missing file    -> " + e.getClass().getSimpleName());
            }
        } finally {
            deleteRecursively(root);
            System.out.println("temp dir removed?    " + !Files.exists(root));
        }
    }

    /** Delete children before parents: reverse order of walk. */
    static void deleteRecursively(Path root) throws IOException {
        try (Stream<Path> s = Files.walk(root)) {
            List<Path> all = s.sorted(Comparator.reverseOrder()).toList();
            for (Path p : all) Files.delete(p);
        }
    }
}
