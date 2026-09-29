package lld.ch01.playlist;

import java.util.ArrayList;
import java.util.List;

/** Holds songs in order. It stores them; it does not play them. */
public final class Playlist {
    private final String name;
    private final List<Song> songs = new ArrayList<>();   // composition: the playlist owns its list

    public Playlist(String name) { this.name = name; }

    public void add(Song song) { songs.add(song); }
    public int size()          { return songs.size(); }
    public Song get(int position) { return songs.get(position); }
    public String name()       { return name; }
}
