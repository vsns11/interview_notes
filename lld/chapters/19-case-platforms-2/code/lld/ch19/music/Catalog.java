package lld.ch19.music;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

enum Genre { ROCK, POP, JAZZ, CLASSICAL, HIPHOP }

record Artist(String id, String name) { }

record Album(String id, String title, Artist artist, int year) { }

record Song(String id, String title, Artist artist, Album album, Genre genre, int durationSec) {
    @Override public String toString() { return title + " - " + artist.name(); }
}

/** The shared, read-mostly catalog. Thread-safe collections because every user reads it at once. */
final class MusicLibrary {
    private final Map<String, Song> songs = new ConcurrentHashMap<>();
    private final Map<String, Album> albums = new ConcurrentHashMap<>();
    private final Map<String, Artist> artists = new ConcurrentHashMap<>();

    void add(Song s) {
        songs.put(s.id(), s);
        albums.putIfAbsent(s.album().id(), s.album());
        artists.putIfAbsent(s.artist().id(), s.artist());
    }

    Song song(String id) { return songs.get(id); }
    List<Song> allSongs() { return sorted(songs.values(), Song::id); }

    List<Song> searchSongs(String q) { return match(songs.values(), q, Song::title); }
    List<Album> searchAlbums(String q) { return match(albums.values(), q, Album::title); }
    List<Artist> searchArtists(String q) { return match(artists.values(), q, Artist::name); }
    List<Song> byArtist(Artist a) { return songs.values().stream().filter(s -> s.artist().equals(a)).sorted(
            (x, y) -> x.id().compareTo(y.id())).toList(); }

    private static <T> List<T> match(Iterable<T> items, String q, Function<T, String> field) {
        String needle = q.toLowerCase(Locale.ROOT);
        List<T> out = new ArrayList<>();
        for (T t : items) if (field.apply(t).toLowerCase(Locale.ROOT).contains(needle)) out.add(t);
        out.sort((a, b) -> field.apply(a).compareTo(field.apply(b)));
        return out;
    }

    private static <T> List<T> sorted(Iterable<T> items, Function<T, String> key) {
        List<T> out = new ArrayList<>();
        items.forEach(out::add);
        out.sort((a, b) -> key.apply(a).compareTo(key.apply(b)));
        return out;
    }
}

/** What each plan allows. The enum carries the rules, so no if (plan == FREE) scattered around. */
enum SubscriptionPlan {
    FREE(new BigDecimal("0.00"), 6, true, false),
    PREMIUM(new BigDecimal("10.99"), Integer.MAX_VALUE, false, true);

    final BigDecimal monthlyPrice;
    final int skipsPerHour;
    final boolean ads;
    final boolean offlineDownloads;

    SubscriptionPlan(BigDecimal price, int skipsPerHour, boolean ads, boolean offline) {
        this.monthlyPrice = price; this.skipsPerHour = skipsPerHour; this.ads = ads; this.offlineDownloads = offline;
    }
}

final class User {
    private final String id;
    private final String name;
    private volatile SubscriptionPlan plan = SubscriptionPlan.FREE;
    private final List<Playlist> playlists = new CopyOnWriteArrayList<>();
    private final List<Song> history = new CopyOnWriteArrayList<>();

    User(String id, String name) { this.id = id; this.name = name; }
    String id() { return id; }
    String name() { return name; }
    SubscriptionPlan plan() { return plan; }

    /** Returns the amount to charge now. */
    BigDecimal subscribe(SubscriptionPlan p) { plan = p; return p.monthlyPrice; }

    Playlist createPlaylist(String name) { Playlist p = new Playlist(name, this); playlists.add(p); return p; }
    List<Playlist> playlists() { return List.copyOf(playlists); }
    void recordPlay(Song s) { history.add(s); }
    List<Song> history() { return Collections.unmodifiableList(history); }
}

/** An ordered, user-owned list of songs. Duplicates are allowed, as in real apps. */
final class Playlist {
    private final String name;
    private final User owner;
    private final List<Song> songs = new ArrayList<>();

    Playlist(String name, User owner) { this.name = name; this.owner = owner; }
    String name() { return name; }
    User owner() { return owner; }
    synchronized Playlist add(Song s) { songs.add(s); return this; }
    synchronized void remove(Song s) { songs.remove(s); }
    synchronized void move(int from, int to) { songs.add(to, songs.remove(from)); }
    synchronized List<Song> songs() { return List.copyOf(songs); }
}
