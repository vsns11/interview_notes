package ch05.immutable;

import java.util.ArrayList;
import java.util.List;

public final class Playlist {
    private final String name;
    private final List<String> songs;

    public Playlist(String name, List<String> songs) {
        this.name = name;
        this.songs = List.copyOf(songs);               // defensive copy IN (and unmodifiable)
    }

    public List<String> songs() { return songs; }      // safe to return: nobody can modify it

    public Playlist with(String song) {                // "wither": copy + change
        List<String> next = new ArrayList<>(songs);
        next.add(song);
        return new Playlist(name, next);
    }

    @Override public String toString() { return name + songs; }
}
