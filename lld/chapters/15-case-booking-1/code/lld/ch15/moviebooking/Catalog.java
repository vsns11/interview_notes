package lld.ch15.moviebooking;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** The five searches. Four are indexed when a movie is added; "by city" is answered from the shows. */
interface MovieSearch {
    List<Movie> searchByTitle(String title);
    List<Movie> searchByLanguage(String language);
    List<Movie> searchByGenre(String genre);
    List<Movie> searchByReleaseDate(LocalDate date);
    List<Movie> searchByCity(String cityName);
}

final class Catalog implements MovieSearch {
    private final Map<String, List<Movie>> byTitle = new ConcurrentHashMap<>();
    private final Map<String, List<Movie>> byLanguage = new ConcurrentHashMap<>();
    private final Map<String, List<Movie>> byGenre = new ConcurrentHashMap<>();
    private final Map<LocalDate, List<Movie>> byRelease = new ConcurrentHashMap<>();
    private final List<Show> shows = new CopyOnWriteArrayList<>();
    private final Notifier notifier;

    Catalog(Notifier notifier) { this.notifier = notifier; }

    /** Admin adds a movie: index it four ways and announce it (requirement: notify on a new movie). */
    void addMovie(Movie m) {
        put(byTitle, key(m.title()), m);
        put(byLanguage, key(m.language()), m);
        put(byGenre, key(m.genre()), m);
        byRelease.computeIfAbsent(m.releaseDate(), d -> new CopyOnWriteArrayList<>()).add(m);
        notifier.send("subscribers", "new movie: " + m.title() + " (" + m.language() + ", " + m.genre() + ")");
    }

    /** Admin schedules a show; the hall refuses it if it overlaps another. */
    boolean addShow(Show s) {
        if (!s.hall().addShow(s)) return false;
        shows.add(s);
        return true;
    }

    /** After picking a movie: which shows play in my city? */
    List<Show> showsOf(Movie m, String cityName) {
        return shows.stream()
                .filter(s -> s.movie().equals(m) && s.cinema().city().name().equalsIgnoreCase(cityName))
                .sorted(Comparator.comparing(Show::start)).toList();
    }

    @Override public List<Movie> searchByTitle(String t)    { return get(byTitle, key(t)); }
    @Override public List<Movie> searchByLanguage(String l) { return get(byLanguage, key(l)); }
    @Override public List<Movie> searchByGenre(String g)    { return get(byGenre, key(g)); }
    @Override public List<Movie> searchByReleaseDate(LocalDate d) { return List.copyOf(byRelease.getOrDefault(d, List.of())); }
    @Override public List<Movie> searchByCity(String city) {
        return shows.stream().filter(s -> s.cinema().city().name().equalsIgnoreCase(city))
                .map(Show::movie).distinct().sorted(Comparator.comparing(Movie::title)).toList();
    }

    private static String key(String s) { return s.trim().toLowerCase(Locale.ROOT); }
    private static void put(Map<String, List<Movie>> idx, String k, Movie m) {
        idx.computeIfAbsent(k, x -> new CopyOnWriteArrayList<>()).add(m);
    }
    private static List<Movie> get(Map<String, List<Movie>> idx, String k) {
        return idx.getOrDefault(k, List.of()).stream().sorted(Comparator.comparing(Movie::title)).toList();
    }
}
