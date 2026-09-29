package lld.ch21.filesearch;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");
    static final long KB = 1024, MB = 1024 * KB;

    public static void main(String[] args) {
        FileSystem fs = sampleTree();
        FileSearcher finder = new FileSearcher(fs);
        FindCommandParser parser = new FindCommandParser(NOW);

        System.out.println("== Builder API ==");
        show(finder, SearchCriteria.from("/").name("*.log").type(FileType.FILE).build());
        show(finder, SearchCriteria.from("/home").where(Filters.iname("*.jpg").and(Filters.size("+1M"))).build());
        show(finder, SearchCriteria.from("/").type(FileType.DIRECTORY).where(Filters.empty()).build());
        show(finder, SearchCriteria.from("/home").maxDepth(2).build());
        show(finder, SearchCriteria.from("/home/asha/projects")
                .where(Filters.name("*.java").or(Filters.name("*.js"))).prune("node_modules").build());
        show(finder, SearchCriteria.from("/").type(FileType.FILE).limit(3).build());

        System.out.println("\n== find command lines ==");
        for (String cmd : List.of(
                "find /var/log -type f -size +10M -mtime +7",
                "find /home -name '*.java' -o -name '*.md'",
                "find /home ( -name '*.jpg' -o -name '*.png' ) ! -size +1M",
                "find /home -size -1M",
                "find / -user ravi -type f",
                "find /home/asha -mindepth 2 -maxdepth 2 -type d",
                "find /home -iname 'b*'",
                "find /var/log -name 'app.log.[0-9]'"))
            show(finder, parser.parse(cmd), cmd);

        System.out.println("\n== bad input gives a clear message ==");
        for (String cmd : List.of("find / -size 10X", "find / ( -name a", "find /nope -name x",
                "find / -frobnicate", "find / -type x", "find / -maxdepth 1 -mindepth 3", "find / -name"))
            try { finder.search(parser.parse(cmd)); System.out.println("   (no error?) " + cmd); }
            catch (SearchException e) { System.out.printf("   %-34s -> %s%n", cmd, e.getMessage()); }

        System.out.println("\n== Composite sizes (du -s) ==");
        for (String p : List.of("/home/asha", "/home/asha/projects/app/node_modules", "/var/log"))
            System.out.printf("   %-40s %,d bytes%n", p, fs.resolve(p).totalSize());

        System.out.println("\n== Parallel search on a big generated tree ==");
        FileSystem big = new FileSystem(NOW);
        for (int d = 0; d < 200; d++)
            for (int f = 0; f < 100; f++)
                big.createFile("/data/d%03d/sub%d/f%03d.%s".formatted(d, f % 5, f, f % 3 == 0 ? "csv" : "txt"),
                        (long) f * 10 * KB, "etl", NOW.minus(Duration.ofDays(f % 30)));
        SearchCriteria q = new FindCommandParser(NOW).parse("find /data -name '*.csv' -size +500k -mtime -10");
        FileSearcher bigFinder = new FileSearcher(big);
        List<Node> seq = bigFinder.search(q);
        List<Node> par = bigFinder.searchParallel(q, ForkJoinPool.commonPool());
        System.out.println("   " + q);
        System.out.println("   sequential=" + seq.size() + " parallel=" + par.size() + " same order=" + seq.equals(par)
                + " first=" + seq.get(0) + " last=" + seq.get(seq.size() - 1));
    }

    static void show(FileSearcher finder, SearchCriteria c) { show(finder, c, c.toString()); }

    static void show(FileSearcher finder, SearchCriteria c, String title) {
        System.out.println("$ " + title);
        if (!title.equals(c.toString())) System.out.println("  parsed: " + c);
        int n = finder.search(c, node -> System.out.println("   " + node + (node instanceof DirectoryNode ? "/" : "")));
        if (n == 0) System.out.println("   (no matches)");
    }

    static FileSystem sampleTree() {
        FileSystem fs = new FileSystem(NOW);
        Instant today = NOW.minus(Duration.ofHours(3)), lastWeek = NOW.minus(Duration.ofDays(8)), old = NOW.minus(Duration.ofDays(40));
        fs.createFile("/home/asha/notes.txt", 2 * KB, "asha", today);
        fs.createFile("/home/asha/photos/a.jpg", 3 * MB, "asha", lastWeek);
        fs.createFile("/home/asha/photos/b.JPG", 5 * MB, "asha", lastWeek);
        fs.createFile("/home/asha/photos/c.png", 800 * KB, "asha", old);
        fs.createFile("/home/asha/photos/d.jpg", 900 * KB, "asha", old);
        fs.createFile("/home/asha/projects/app/Main.java", 12 * KB, "asha", today);
        fs.createFile("/home/asha/projects/app/Util.java", 4 * KB, "asha", today);
        fs.createFile("/home/asha/projects/app/build.log", 3 * MB / 2, "asha", today);
        fs.createFile("/home/asha/projects/app/node_modules/lodash.js", 500 * KB, "asha", old);
        fs.createFile("/home/asha/projects/app/node_modules/react.js", MB, "asha", old);
        fs.mkdirs("/home/asha/projects/empty-dir", "asha", old);
        fs.createFile("/home/ravi/todo.md", 0, "ravi", today);
        fs.createFile("/home/ravi/report.pdf", 2 * MB, "ravi", lastWeek);
        fs.createFile("/var/log/syslog", 40 * MB, "root", old);
        fs.createFile("/var/log/app.log", 3 * MB, "root", today);
        fs.createFile("/var/log/app.log.1", 20 * MB, "root", lastWeek);
        fs.mkdirs("/tmp", "root", today);
        return fs;
    }
}
