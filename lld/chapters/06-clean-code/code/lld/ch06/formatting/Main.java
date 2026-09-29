package lld.ch06.formatting;

import java.util.ArrayList;
import java.util.List;

/** Formatting: same code, formatted two ways. The compiler doesn't care; readers do. */
public class Main {

    // ---------------- BEFORE: cramped, random order, aligned columns, no vertical openness ----------------
    static class Before {
        static class Playlist{private int cursor;
            private String helper(int i){return songs.get(i).toUpperCase();}
            public String next(){if(songs.isEmpty())return null;String s=helper(cursor);cursor=(cursor+1)%songs.size();return s;}
            private final List<String>   songs   =   new ArrayList<>();
            private final String         name;
            public void add(String s){songs.add(s);}Playlist(String n){name=n;}
            public String name(){return name;}}
    }

    // ---------------- AFTER: newspaper order -- headline first, details below ----------------
    static class After {
        /** A looping playlist. (Headline: the class name and this one line tell you what it is.) */
        static final class Playlist {
            // 1. Fields at the top, together (vertical density for closely related lines).
            private final String name;
            private final List<String> songs = new ArrayList<>();
            private int cursor;

            // 2. Constructor next.
            Playlist(String name) {
                this.name = name;
            }

            // 3. Public API -- each concept separated by a blank line (vertical openness).
            public String name() {
                return name;
            }

            public void add(String song) {
                songs.add(song);
            }

            public String next() {
                if (songs.isEmpty()) {
                    return null;
                }
                String song = displayTitle(cursor);    // caller ABOVE callee (dependent functions close together)
                cursor = (cursor + 1) % songs.size();
                return song;
            }

            // 4. Private helpers just below the functions that use them.
            private String displayTitle(int index) {
                return songs.get(index).toUpperCase();
            }
        }
    }

    public static void main(String[] args) {
        var before = new Before.Playlist("Road trip");
        var after = new After.Playlist("Road trip");
        for (String s : List.of("Kesariya", "Yellow", "Tum Hi Ho")) { before.add(s); after.add(s); }
        StringBuilder b = new StringBuilder(), a = new StringBuilder();
        for (int i = 0; i < 4; i++) { b.append(before.next()).append(' '); a.append(after.next()).append(' '); }
        System.out.println("BEFORE " + before.name() + ": " + b.toString().trim());
        System.out.println("AFTER  " + after.name() + ": " + a.toString().trim());
        System.out.println("Same behaviour -- formatting only changes how fast a human understands it.");
    }
}
