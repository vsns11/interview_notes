package lld.ch01.playlist;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Playlist mix = new Playlist("Morning mix");
        mix.add(new Song("Sunrise", "Ada Lane", 201));
        mix.add(new Song("Blue Line", "The Meridians", 187));
        mix.add(new Song("Paper Boats", "Nori Kim", 243));

        run("in order", new Player(mix, new InOrder()), mix.size());
        run("shuffled", new Player(mix, new Shuffled(List.of(2, 0, 1))), mix.size());
    }

    private static void run(String label, Player player, int songs) {
        System.out.println(label + ":");
        for (int i = 0; i <= songs; i++) {          // one extra call shows the end
            System.out.println("  " + player.playNext());
        }
    }
}
