package lld.ch21.filesearch;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListMap;

public final class DirectoryNode extends Node {
    /** Sorted by name (deterministic output) and safe to read while another thread adds files. */
    private final ConcurrentSkipListMap<String, Node> children = new ConcurrentSkipListMap<>();

    DirectoryNode(String name, DirectoryNode parent, String owner, Instant modified) {
        super(name, parent, owner, modified);
    }

    @Override public FileType type() { return FileType.DIRECTORY; }
    @Override public long size() { return 0; }
    @Override public long totalSize() { return children.values().stream().mapToLong(Node::totalSize).sum(); }

    public Collection<Node> children() { return children.values(); }
    public Node child(String name) { return children.get(name); }
    public boolean isEmpty() { return children.isEmpty(); }

    void add(Node n) {
        if (children.putIfAbsent(n.name(), n) != null)
            throw new SearchException(n.path() + " already exists");
    }
}
