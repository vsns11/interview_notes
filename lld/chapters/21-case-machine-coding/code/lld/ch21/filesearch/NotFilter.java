package lld.ch21.filesearch;

/** Composite with one child: find's ! / -not. */
public record NotFilter(FileFilter inner) implements FileFilter {
    @Override public boolean matches(Node n) { return !inner.matches(n); }
    @Override public String describe() { return "NOT " + inner.describe(); }
}
