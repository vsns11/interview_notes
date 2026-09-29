package lld.ch21.filesearch;

import java.time.Instant;

/** A small in-memory file system, so every demo run is identical. A java.nio adapter is an extension. */
public final class FileSystem {
    private final DirectoryNode root;

    public FileSystem(Instant created) { root = new DirectoryNode("", null, "root", created); }

    public DirectoryNode root() { return root; }

    /** Creates missing directories along the way, like mkdir -p. */
    public synchronized DirectoryNode mkdirs(String path, String owner, Instant modified) {
        DirectoryNode dir = root;
        for (String part : split(path)) {
            Node next = dir.child(part);
            if (next == null) { next = new DirectoryNode(part, dir, owner, modified); dir.add(next); }
            if (!(next instanceof DirectoryNode d)) throw new SearchException(next.path() + " is a file, not a directory");
            dir = d;
        }
        return dir;
    }

    public synchronized FileNode createFile(String path, long size, String owner, Instant modified) {
        int slash = path.lastIndexOf('/');
        DirectoryNode dir = mkdirs(path.substring(0, Math.max(slash, 0)), owner, modified);
        FileNode f = new FileNode(path.substring(slash + 1), dir, owner, modified, size);
        dir.add(f);
        return f;
    }

    public Node resolve(String path) {
        Node n = root;
        for (String part : split(path)) {
            if (!(n instanceof DirectoryNode d) || (n = d.child(part)) == null)
                throw new SearchException("No such file or directory: " + path);
        }
        return n;
    }

    private static String[] split(String path) {
        String p = path.replaceAll("^/+|/+$", "");
        return p.isEmpty() ? new String[0] : p.split("/+");
    }
}
