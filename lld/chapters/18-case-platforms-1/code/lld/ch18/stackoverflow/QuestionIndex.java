package lld.ch18.stackoverflow;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Search contract, kept separate so a full-text engine could replace the in-memory index. */
interface Search {
    List<Question> search(String query);
    List<Question> searchByTag(String tagName);
}

/**
 * An inverted index: word -> questions containing it. It refers to questions
 * but does not own them (throwing the index away deletes no question).
 */
final class QuestionIndex implements Search {
    private final Map<String, Set<Question>> byWord = new ConcurrentHashMap<>();
    private final Map<String, Set<Question>> byTag = new ConcurrentHashMap<>();

    void add(Question q) {
        for (String word : words(q.title() + " " + q.body())) {
            byWord.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet()).add(q);
        }
        for (Tag t : q.tags()) {
            byTag.computeIfAbsent(t.name(), k -> ConcurrentHashMap.newKeySet()).add(q);
        }
    }

    /** Questions containing every word of the query, best score first; deleted questions are hidden. */
    @Override public List<Question> search(String query) {
        List<String> terms = words(query);
        if (terms.isEmpty()) return List.of();
        Set<Question> result = new HashSet<>(byWord.getOrDefault(terms.get(0), Set.of()));
        for (String t : terms.subList(1, terms.size())) result.retainAll(byWord.getOrDefault(t, Set.of()));
        return rank(result);
    }

    @Override public List<Question> searchByTag(String tagName) {
        return rank(byTag.getOrDefault(tagName.toLowerCase(Locale.ROOT), Set.of()));
    }

    private static List<Question> rank(Set<Question> qs) {
        return qs.stream()
                .filter(q -> q.status() != QuestionStatus.DELETED)
                .sorted(Comparator.comparingInt(Question::score).reversed().thenComparingLong(Question::id))
                .toList();
    }

    private static List<String> words(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9#+]+"))
                .filter(w -> w.length() > 2)
                .distinct()
                .toList();
    }
}
