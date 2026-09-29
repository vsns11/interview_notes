package lld.ch21.filesearch;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Everything one search needs, immutable once built. Builder pattern: most options are optional,
 * so named builder calls replace a constructor with eight positional parameters.
 */
public final class SearchCriteria {
    private final String startPath;
    private final FileFilter filter;
    private final int minDepth, maxDepth, limit;
    private final Set<String> pruneNames;

    private SearchCriteria(Builder b) {
        startPath = b.startPath; filter = b.filter; minDepth = b.minDepth; maxDepth = b.maxDepth;
        limit = b.limit; pruneNames = Set.copyOf(b.pruneNames);
    }

    public static Builder from(String startPath) { return new Builder(startPath); }

    public String startPath() { return startPath; }
    public FileFilter filter() { return filter; }
    public int minDepth() { return minDepth; }
    public int maxDepth() { return maxDepth; }
    public int limit() { return limit; }
    public Set<String> pruneNames() { return pruneNames; }

    @Override public String toString() {
        return "from " + startPath + " where " + filter.describe()
                + (minDepth > 0 ? " mindepth=" + minDepth : "")
                + (maxDepth < Integer.MAX_VALUE ? " maxdepth=" + maxDepth : "")
                + (pruneNames.isEmpty() ? "" : " prune=" + pruneNames)
                + (limit < Integer.MAX_VALUE ? " limit=" + limit : "");
    }

    public static final class Builder {
        private final String startPath;
        private FileFilter filter = Filters.all();
        private boolean filterSet;
        private int minDepth = 0, maxDepth = Integer.MAX_VALUE, limit = Integer.MAX_VALUE;
        private final Set<String> pruneNames = new LinkedHashSet<>();

        private Builder(String startPath) {
            if (startPath == null || startPath.isBlank()) throw new SearchException("Start path is required");
            this.startPath = startPath;
        }

        /** Each call is ANDed with the previous ones, like consecutive tests on a find command line. */
        public Builder where(FileFilter f) { filter = filterSet ? filter.and(f) : f; filterSet = true; return this; }
        public Builder name(String glob) { return where(Filters.name(glob)); }
        public Builder type(FileType t) { return where(Filters.type(t)); }
        public Builder size(String spec) { return where(Filters.size(spec)); }
        public Builder minDepth(int d) { minDepth = d; return this; }
        public Builder maxDepth(int d) { maxDepth = d; return this; }
        public Builder limit(int n) { limit = n; return this; }
        /** Do not descend into directories with this name (like: -name node_modules -prune -o ...). */
        public Builder prune(String dirName) { pruneNames.add(dirName); return this; }

        public SearchCriteria build() {
            if (minDepth < 0 || maxDepth < 0) throw new SearchException("Depths must be >= 0");
            if (minDepth > maxDepth) throw new SearchException("mindepth " + minDepth + " > maxdepth " + maxDepth);
            if (limit <= 0) throw new SearchException("Limit must be positive");
            return new SearchCriteria(this);
        }
    }
}
