package lld.ch19.music;

import java.time.Instant;
import java.time.InstantSource;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MusicLibrary lib = new MusicLibrary();
        Artist queen = new Artist("AR1", "Queen");
        Artist miles = new Artist("AR2", "Miles Davis");
        Artist adele = new Artist("AR3", "Adele");
        Album opera = new Album("AL1", "A Night at the Opera", queen, 1975);
        Album blue = new Album("AL2", "Kind of Blue", miles, 1959);
        Album twentyFive = new Album("AL3", "25", adele, 2015);
        lib.add(new Song("S1", "Bohemian Rhapsody", queen, opera, Genre.ROCK, 354));
        lib.add(new Song("S2", "Love of My Life", queen, opera, Genre.ROCK, 219));
        lib.add(new Song("S3", "So What", miles, blue, Genre.JAZZ, 562));
        lib.add(new Song("S4", "Blue in Green", miles, blue, Genre.JAZZ, 337));
        lib.add(new Song("S5", "Hello", adele, twentyFive, Genre.POP, 295));
        lib.add(new Song("S6", "Death on Two Legs", queen, opera, Genre.ROCK, 223));
        lib.add(new Song("S7", "Freddie Freeloader", miles, blue, Genre.JAZZ, 589));
        lib.add(new Song("S8", "All Blues", miles, blue, Genre.JAZZ, 693));
        lib.add(new Song("S9", "When We Were Young", adele, twentyFive, Genre.POP, 291));
        lib.add(new Song("S10", "You're My Best Friend", queen, opera, Genre.ROCK, 172));

        System.out.println("== 1. Search ==");
        System.out.println("  songs 'blue'    -> " + lib.searchSongs("blue"));
        System.out.println("  albums 'opera'  -> " + lib.searchAlbums("opera").stream().map(Album::title).toList());
        System.out.println("  artists 'da'    -> " + lib.searchArtists("da").stream().map(Artist::name).toList());

        System.out.println("\n== 2. Playlist ==");
        User mia = new User("U1", "Mia");
        Playlist mix = mia.createPlaylist("Road trip")
                .add(lib.song("S1")).add(lib.song("S3")).add(lib.song("S5")).add(lib.song("S2")).add(lib.song("S4"));
        mix.move(4, 1);
        System.out.println("  " + mix.name() + ": " + mix.songs());

        System.out.println("\n== 3. Play in order, pause, seek, repeat-one ==");
        MusicPlayer player = new MusicPlayer(mia, InstantSource.fixed(Instant.parse("2026-09-28T08:00:00Z")));
        player.addListener((u, s) -> u.recordPlay(s));          // history is just another listener
        player.load(mix.songs());
        player.play();
        player.seek(120);
        player.pause();
        System.out.println("  state " + player.state() + " at " + player.position() + "s");
        player.resume();
        player.setRepeat(RepeatMode.ONE);
        player.trackFinished();                                   // replays the same song
        player.setRepeat(RepeatMode.OFF);
        player.trackFinished();

        System.out.println("\n== 4. Shuffle keeps the current song, reorders the rest ==");
        player.setShuffle(true);
        System.out.println("  now " + player.current() + ", up next " + player.upNext(4));

        System.out.println("\n== 5. FREE plan: skip limit (6/hour) and ads, repeat-all wraps the shuffled queue ==");
        player.setRepeat(RepeatMode.ALL);
        for (int i = 0; i < 7; i++) player.skip();

        System.out.println("\n== 6. Upgrade to PREMIUM: no ads, unlimited skips ==");
        System.out.println("  charged $" + mia.subscribe(SubscriptionPlan.PREMIUM));
        player.setShuffle(false);
        player.load(List.of(lib.song("S6"), lib.song("S7")));
        player.setRepeat(RepeatMode.ALL);
        player.play();
        player.skip();
        player.skip();                                            // wraps to the start
        player.setRepeat(RepeatMode.OFF);
        player.trackFinished();
        player.trackFinished();                                   // end of queue

        System.out.println("\n== 7. Recommendations from listening history ==");
        System.out.println("  plays so far: " + mia.history().size());
        System.out.println("  recommended: " + new GenreRecommender(lib).recommend(mia, 3));
    }
}
