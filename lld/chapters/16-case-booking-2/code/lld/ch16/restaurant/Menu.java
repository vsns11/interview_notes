package lld.ch16.restaurant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A branch's menu: Menu -> sections -> items (composition). Prices live ONLY on MenuItem. */
final class Menu {
    private final Map<String, MenuSection> sections = new LinkedHashMap<>();

    synchronized MenuSection section(String title) { return sections.computeIfAbsent(title, MenuSection::new); }

    synchronized Optional<MenuItem> find(String itemId) {
        return sections.values().stream().flatMap(s -> s.items().stream())
                .filter(i -> i.id().equals(itemId)).findFirst();
    }

    synchronized List<MenuSection> sections() { return List.copyOf(sections.values()); }
}

final class MenuSection {
    private final String title;
    private final List<MenuItem> items = new ArrayList<>();
    MenuSection(String title) { this.title = title; }
    String title() { return title; }
    synchronized void add(MenuItem item) { items.add(item); }
    synchronized boolean remove(String itemId) { return items.removeIf(i -> i.id().equals(itemId)); }
    synchronized List<MenuItem> items() { return List.copyOf(items); }
}
