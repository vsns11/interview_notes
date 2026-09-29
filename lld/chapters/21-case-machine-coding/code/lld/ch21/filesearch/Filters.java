package lld.ch21.filesearch;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

/** Static factories for the leaf filters (one per find test). New test = one new record here; nothing else changes. */
public final class Filters {
    private Filters() { }

    public static FileFilter all() { return new MatchAll(); }
    public static FileFilter name(String glob) { return new NameFilter(glob, false); }
    public static FileFilter iname(String glob) { return new NameFilter(glob, true); }
    public static FileFilter type(FileType t) { return new TypeFilter(t); }
    public static FileFilter owner(String user) { return new OwnerFilter(user); }
    public static FileFilter empty() { return new EmptyFilter(); }
    /** spec like find's -size: "+10M", "-1k", "0c", "20" (no suffix = 512-byte blocks). */
    public static FileFilter size(String spec) { return SizeFilter.parse(spec); }
    /** spec like find's -mtime: "+7" (more than 7 whole days ago), "-1", "0". */
    public static FileFilter mtime(String spec, Instant now) { return MtimeFilter.parse(spec, now); }

    record MatchAll() implements FileFilter {
        @Override public boolean matches(Node n) { return true; }
        @Override public String describe() { return "true"; }
    }

    /** Glob on the last path component only, like find -name: * ? [abc] [!abc], and \ to escape. */
    record NameFilter(String glob, boolean ignoreCase, Pattern regex) implements FileFilter {
        NameFilter(String glob, boolean ignoreCase) {
            this(glob, ignoreCase, Pattern.compile(toRegex(glob), ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0));
        }
        @Override public boolean matches(Node n) { return regex.matcher(n.name()).matches(); }
        @Override public String describe() { return (ignoreCase ? "iname='" : "name='") + glob + "'"; }

        static String toRegex(String glob) {
            StringBuilder re = new StringBuilder();
            for (int i = 0; i < glob.length(); i++) {
                char c = glob.charAt(i);
                switch (c) {
                    case '*' -> re.append(".*");
                    case '?' -> re.append('.');
                    case '\\' -> { if (++i < glob.length()) re.append(Pattern.quote(String.valueOf(glob.charAt(i)))); }
                    case '[' -> {
                        int end = glob.indexOf(']', i + 2);
                        if (end < 0) { re.append("\\["); break; }          // no closing ]: a literal [
                        String body = glob.substring(i + 1, end);
                        if (body.startsWith("!")) body = "^" + body.substring(1);
                        re.append('[').append(body.replace("\\", "\\\\")).append(']');
                        i = end;
                    }
                    default -> re.append(Pattern.quote(String.valueOf(c)));
                }
            }
            return re.toString();
        }
    }

    record TypeFilter(FileType type) implements FileFilter {
        @Override public boolean matches(Node n) { return n.type() == type; }
        @Override public String describe() { return "type=" + (type == FileType.FILE ? "f" : "d"); }
    }

    record OwnerFilter(String user) implements FileFilter {
        @Override public boolean matches(Node n) { return n.owner().equals(user); }
        @Override public String describe() { return "user=" + user; }
    }

    record EmptyFilter() implements FileFilter {
        @Override public boolean matches(Node n) {
            return n instanceof DirectoryNode d ? d.isEmpty() : n.size() == 0;
        }
        @Override public String describe() { return "empty"; }
    }

    /**
     * GNU find semantics: the file size is rounded UP to whole units, then compared.
     * So "-size -1M" means "fewer than 1 whole MiB after rounding up" = only empty files. A famous gotcha.
     * Size tests apply to files only in this model.
     */
    record SizeFilter(char cmp, long n, char unit, String spec) implements FileFilter {
        static SizeFilter parse(String spec) {
            var m = Pattern.compile("([+-]?)(\\d+)([cbkMG]?)").matcher(spec);
            if (!m.matches()) throw new SearchException("Invalid -size '" + spec + "' (use e.g. +10M, -1k, 100c)");
            char cmp = m.group(1).isEmpty() ? '=' : m.group(1).charAt(0);
            char unit = m.group(3).isEmpty() ? 'b' : m.group(3).charAt(0);
            return new SizeFilter(cmp, Long.parseLong(m.group(2)), unit, spec);
        }
        long unitBytes() {
            return switch (unit) { case 'c' -> 1; case 'b' -> 512; case 'k' -> 1024; case 'M' -> 1024L * 1024; default -> 1024L * 1024 * 1024; };
        }
        @Override public boolean matches(Node node) {
            if (node.type() != FileType.FILE) return false;
            long units = (node.size() + unitBytes() - 1) / unitBytes();      // round up
            return switch (cmp) { case '+' -> units > n; case '-' -> units < n; default -> units == n; };
        }
        @Override public String describe() { return "size" + (cmp == '=' ? "=" : "") + spec; }
    }

    /** -mtime n: age in whole days (fraction dropped); +n = older than n days, -n = newer than n days. */
    record MtimeFilter(char cmp, long days, Instant now, String spec) implements FileFilter {
        static MtimeFilter parse(String spec, Instant now) {
            var m = Pattern.compile("([+-]?)(\\d+)").matcher(spec);
            if (!m.matches()) throw new SearchException("Invalid -mtime '" + spec + "' (use e.g. +7, -1, 0)");
            char cmp = m.group(1).isEmpty() ? '=' : m.group(1).charAt(0);
            return new MtimeFilter(cmp, Long.parseLong(m.group(2)), now, spec);
        }
        @Override public boolean matches(Node node) {
            long age = Duration.between(node.modified(), now).toDays();
            return switch (cmp) { case '+' -> age > days; case '-' -> age < days; default -> age == days; };
        }
        @Override public String describe() { return "mtime" + (cmp == '=' ? "=" : "") + spec; }
    }
}
