package lld.ch01.playlist;

/** A song knows about itself and nothing else. A record = immutable data carrier. */
public record Song(String title, String artist, int seconds) {
    public Song {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (seconds <= 0) throw new IllegalArgumentException("length must be positive");
    }
}
