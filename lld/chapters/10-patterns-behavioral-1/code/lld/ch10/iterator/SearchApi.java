package lld.ch10.iterator;

import java.util.List;

/** A remote source that returns two results per page and knows nothing about iteration. */
final class SearchApi {
    private static final List<String> ALL = List.of("result-1", "result-2", "result-3", "result-4", "result-5");
    private static final int PAGE_SIZE = 2;
    private int calls = 0;

    List<String> fetchPage(int page) {          // pages are 1-based
        calls++;
        System.out.println("    [api] fetching page " + page);
        int from = (page - 1) * PAGE_SIZE;
        if (from >= ALL.size()) return List.of();
        return ALL.subList(from, Math.min(from + PAGE_SIZE, ALL.size()));
    }

    int calls() { return calls; }
}
