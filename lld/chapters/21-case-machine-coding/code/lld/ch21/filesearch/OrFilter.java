package lld.ch21.filesearch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Composite: matches when any child matches. Short-circuits like find's -o. */
public record OrFilter(List<FileFilter> parts) implements FileFilter {
    public OrFilter { parts = List.copyOf(parts); }

    static FileFilter of(FileFilter a, FileFilter b) {
        List<FileFilter> all = new ArrayList<>();
        for (FileFilter f : List.of(a, b)) if (f instanceof OrFilter or) all.addAll(or.parts()); else all.add(f);
        return new OrFilter(all);
    }

    @Override public boolean matches(Node n) {
        for (FileFilter f : parts) if (f.matches(n)) return true;
        return false;
    }

    @Override public String describe() {
        return parts.stream().map(FileFilter::describe).collect(Collectors.joining(" OR ", "(", ")"));
    }
}
