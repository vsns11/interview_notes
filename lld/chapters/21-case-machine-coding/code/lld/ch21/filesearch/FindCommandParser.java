package lld.ch21.filesearch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Turns a find-style command line into SearchCriteria, with find's precedence:
 *   expr   := or
 *   or     := and ( ("-o" | "-or") and )*
 *   and    := unary ( ["-a" | "-and"] unary )*      -- two tests side by side mean AND
 *   unary  := ("!" | "-not") unary | "(" expr ")" | test
 *   test   := -name G | -iname G | -type f|d | -size N | -mtime N | -user U | -empty | -true
 * Global options -maxdepth N / -mindepth N may appear anywhere. This is recursive descent:
 * one method per grammar rule, each returning a FileFilter (the Composite tree).
 */
public final class FindCommandParser {
    private final Instant now;
    private List<String> tokens;
    private int pos;
    private SearchCriteria.Builder builder;

    public FindCommandParser(Instant now) { this.now = now; }

    /** tokens as the shell would pass them, e.g. ["find", "/home", "-name", "*.java", "-o", "-name", "*.md"]. */
    public SearchCriteria parse(List<String> args) {
        if (args.isEmpty() || !args.get(0).equals("find")) throw new SearchException("Command must start with 'find'");
        tokens = args; pos = 1;
        String start = pos < tokens.size() && !isOperatorOrTest(tokens.get(pos)) ? tokens.get(pos++) : ".";
        builder = SearchCriteria.from(start.equals(".") ? "/" : start);
        skipGlobalOptions();
        if (pos < tokens.size()) {                     // no tests at all = match everything
            builder.where(parseOr());
            if (pos < tokens.size()) throw new SearchException("Unexpected '" + tokens.get(pos) + "'");
        }
        return builder.build();
    }

    /** Convenience: split on spaces; quotes group words and are removed (enough for the demo). */
    public SearchCriteria parse(String commandLine) {
        List<String> out = new ArrayList<>();
        var m = Pattern.compile("'([^']*)'|\"([^\"]*)\"|(\\S+)").matcher(commandLine);
        while (m.find()) out.add(m.group(1) != null ? m.group(1) : m.group(2) != null ? m.group(2) : m.group(3));
        return parse(out);
    }

    private FileFilter parseOr() {
        FileFilter left = parseAnd();
        while (peekIs("-o", "-or")) { pos++; left = left.or(parseAnd()); }
        return left;
    }

    private FileFilter parseAnd() {
        FileFilter left = parseUnary();
        while (true) {
            skipGlobalOptions();
            if (pos >= tokens.size() || peekIs("-o", "-or", ")")) return left;
            if (peekIs("-a", "-and")) pos++;
            left = left.and(parseUnary());
        }
    }

    private FileFilter parseUnary() {
        skipGlobalOptions();
        if (pos >= tokens.size()) throw new SearchException("Expression ends too early");
        String t = tokens.get(pos++);
        switch (t) {
            case "!", "-not" -> { return parseUnary().negate(); }
            case "(" -> {
                FileFilter inner = parseOr();
                if (!peekIs(")")) throw new SearchException("Missing ')'");
                pos++;
                return inner;
            }
            case ")" -> throw new SearchException("Unexpected ')'");
            case "-empty" -> { return Filters.empty(); }
            case "-true" -> { return Filters.all(); }
            case "-name" -> { return Filters.name(arg(t)); }
            case "-iname" -> { return Filters.iname(arg(t)); }
            case "-user" -> { return Filters.owner(arg(t)); }
            case "-size" -> { return Filters.size(arg(t)); }
            case "-mtime" -> { return Filters.mtime(arg(t), now); }
            case "-type" -> {
                String v = arg(t);
                return switch (v) {
                    case "f" -> Filters.type(FileType.FILE);
                    case "d" -> Filters.type(FileType.DIRECTORY);
                    default -> throw new SearchException("-type takes f or d, not '" + v + "'");
                };
            }
            default -> throw new SearchException("Unknown predicate '" + t + "'");
        }
    }

    /** -maxdepth/-mindepth are options, not tests: record them and keep parsing the expression. */
    private void skipGlobalOptions() {
        while (peekIs("-maxdepth", "-mindepth")) {
            String opt = tokens.get(pos++);
            int n;
            try { n = Integer.parseInt(arg(opt)); } catch (NumberFormatException e) { throw new SearchException(opt + " needs a number"); }
            if (opt.equals("-maxdepth")) builder.maxDepth(n); else builder.minDepth(n);
        }
    }

    private String arg(String option) {
        if (pos >= tokens.size()) throw new SearchException(option + " needs an argument");
        return tokens.get(pos++);
    }

    private boolean peekIs(String... options) {
        if (pos >= tokens.size()) return false;
        for (String o : options) if (tokens.get(pos).equals(o)) return true;
        return false;
    }

    private static boolean isOperatorOrTest(String t) { return t.startsWith("-") || t.equals("!") || t.equals("(") || t.equals(")"); }
}
