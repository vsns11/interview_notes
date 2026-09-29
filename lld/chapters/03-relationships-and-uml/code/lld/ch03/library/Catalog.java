package lld.ch03.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** An index over copies. It aggregates BookItems: it points at them but does not create or own them. */
public final class Catalog {
    private final Map<String, List<BookItem>> byTitle = new HashMap<>();

    void index(BookItem item) {
        byTitle.computeIfAbsent(key(item.book().title()), k -> new ArrayList<>()).add(item);
    }

    public List<BookItem> search(String title) {
        List<BookItem> hits = lookUpTitleIndex(title);   // self-call in the sequence diagram
        return List.copyOf(hits);
    }

    private List<BookItem> lookUpTitleIndex(String title) {
        return byTitle.getOrDefault(key(title), List.of());
    }

    private static String key(String title) { return title.toLowerCase(Locale.ROOT); }
}
