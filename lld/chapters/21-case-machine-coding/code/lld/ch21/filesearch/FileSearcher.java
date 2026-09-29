package lld.ch21.filesearch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;

/** Walks the tree and reports every entry that passes the filter. */
public final class FileSearcher {
    private final FileSystem fs;

    public FileSearcher(FileSystem fs) { this.fs = fs; }

    private record Frame(Node node, int depth) { }

    /**
     * Pre-order depth-first walk with an explicit stack (no recursion, so a very deep tree cannot
     * overflow the call stack). Results are streamed to the consumer as they are found, like find
     * printing line by line; nothing forces the whole result list into memory.
     */
    public int search(SearchCriteria c, Consumer<Node> out) {
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(fs.resolve(c.startPath()), 0));
        int found = 0;
        while (!stack.isEmpty() && found < c.limit()) {
            Frame f = stack.pop();
            Node n = f.node();
            if (f.depth() >= c.minDepth() && c.filter().matches(n)) { out.accept(n); found++; }
            if (n instanceof DirectoryNode d && f.depth() < c.maxDepth() && !c.pruneNames().contains(d.name())) {
                List<Node> kids = new ArrayList<>(d.children());
                Collections.reverse(kids);                     // push in reverse so we pop in name order
                for (Node k : kids) stack.push(new Frame(k, f.depth() + 1));
            }
        }
        return found;
    }

    public List<Node> search(SearchCriteria c) {
        List<Node> out = new ArrayList<>();
        search(c, out::add);
        return out;
    }

    /** Extension: one fork/join task per directory. Same results in the same order; no limit support. */
    public List<Node> searchParallel(SearchCriteria c, ForkJoinPool pool) {
        return pool.invoke(new Walk(fs.resolve(c.startPath()), 0, c));
    }

    private static final class Walk extends RecursiveTask<List<Node>> {
        private final Node node; private final int depth; private final SearchCriteria c;
        Walk(Node node, int depth, SearchCriteria c) { this.node = node; this.depth = depth; this.c = c; }

        @Override protected List<Node> compute() {
            List<Node> out = new ArrayList<>();
            if (depth >= c.minDepth() && c.filter().matches(node)) out.add(node);
            if (node instanceof DirectoryNode d && depth < c.maxDepth() && !c.pruneNames().contains(d.name())) {
                List<Walk> tasks = new ArrayList<>();
                List<Object> slots = new ArrayList<>();         // files tested inline, sub-directories forked
                for (Node k : d.children()) {
                    if (k instanceof DirectoryNode) { Walk w = new Walk(k, depth + 1, c); tasks.add(w); slots.add(w); }
                    else slots.add(k);
                }
                invokeAll(tasks);                               // fork all sub-directories, wait for all
                for (Object s : slots) {                        // collect in name order: same order as search()
                    if (s instanceof Walk w) out.addAll(w.join());
                    else if (depth + 1 >= c.minDepth() && c.filter().matches((Node) s)) out.add((Node) s);
                }
            }
            return out;
        }
    }
}
