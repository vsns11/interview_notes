package lld.ch21.filesearch;

import java.time.Instant;

/**
 * Composite pattern: a file-system entry is either a leaf (FileNode) or a container
 * (DirectoryNode) that holds more entries. Callers treat both through this one type.
 */
public abstract sealed class Node permits FileNode, DirectoryNode {
    private final String name;
    private final DirectoryNode parent;
    private final String owner;
    private final Instant modified;

    protected Node(String name, DirectoryNode parent, String owner, Instant modified) {
        this.name = name; this.parent = parent; this.owner = owner; this.modified = modified;
    }

    public String name() { return name; }
    public DirectoryNode parent() { return parent; }
    public String owner() { return owner; }
    public Instant modified() { return modified; }

    public abstract FileType type();

    /** Size in bytes of this entry alone (0 for a directory, as far as our filters care). */
    public abstract long size();

    /** du-style size: a file's own size; a directory's is the sum over everything inside it (Composite recursion). */
    public abstract long totalSize();

    public String path() {
        if (parent == null) return "/";
        String p = parent.path();
        return (p.equals("/") ? "" : p) + "/" + name;
    }

    @Override public String toString() { return path(); }
}
