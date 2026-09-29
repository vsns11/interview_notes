package lld.ch21.filesearch;

import java.time.Instant;

public final class FileNode extends Node {
    private final long size;

    FileNode(String name, DirectoryNode parent, String owner, Instant modified, long size) {
        super(name, parent, owner, modified);
        if (size < 0) throw new SearchException("Negative size for " + name);
        this.size = size;
    }

    @Override public FileType type() { return FileType.FILE; }
    @Override public long size() { return size; }
    @Override public long totalSize() { return size; }
}
