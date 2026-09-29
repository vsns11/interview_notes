package ch05.immutable;

import java.util.ArrayList;
import java.util.List;

public class ImmutabilityDemo {

    static final class LeakyPlaylist {                  // looks immutable, but is not
        private final List<String> songs;
        LeakyPlaylist(List<String> songs) { this.songs = songs; }   // stores the caller's list
        List<String> songs() { return songs; }                      // hands out the internal list
    }

    public static void main(String[] args) {
        Money price = new Money(1999, "EUR");
        Money total = price.plus(new Money(501, "EUR"));
        System.out.println(price + " + 5.01 EUR = " + total + " (price unchanged: " + price + ")");

        List<String> input = new ArrayList<>(List.of("Intro", "Verse"));
        LeakyPlaylist leaky = new LeakyPlaylist(input);
        input.add("SNEAKY");                             // mutate after construction
        leaky.songs().clear();                           // or mutate through the getter
        System.out.println("leaky after outside changes: " + leaky.songs());

        List<String> input2 = new ArrayList<>(List.of("Intro", "Verse"));
        Playlist safe = new Playlist("Mix", input2);
        input2.add("SNEAKY");
        System.out.println("safe after outside change:   " + safe);
        try {
            safe.songs().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("safe.songs().clear() -> UnsupportedOperationException");
        }
        System.out.println("with(\"Chorus\"): " + safe.with("Chorus") + ", original: " + safe);

        final StringBuilder sb = new StringBuilder("final ref");
        sb.append(" but mutable object");                // final stops reassignment, not mutation
        System.out.println(sb);
    }
}
