package lld.ch19.music;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/** Strategy: in what order should the queue be played? */
interface PlaybackOrder {
    /** A permutation of 0..size-1 that starts with {@code first}. */
    List<Integer> order(int size, int first);
}

final class SequentialOrder implements PlaybackOrder {
    @Override public List<Integer> order(int size, int first) {
        List<Integer> out = new ArrayList<>(IntStream.range(0, size).boxed().toList());
        Collections.rotate(out, -first);        // start at the current track, keep the rest in order
        return out;
    }
}

final class ShuffleOrder implements PlaybackOrder {
    private final Random random;
    ShuffleOrder(long seed) { this.random = new Random(seed); }   // seeded: repeatable demo and tests
    @Override public List<Integer> order(int size, int first) {
        List<Integer> rest = new ArrayList<>(IntStream.range(0, size).filter(i -> i != first).boxed().toList());
        Collections.shuffle(rest, random);
        rest.add(0, first);                     // the song you are hearing keeps playing
        return rest;
    }
}

enum RepeatMode { OFF, ALL, ONE }

enum PlayerState { STOPPED, PLAYING, PAUSED }

/** Observer hook: history, royalties, recommendations all listen to plays. */
interface PlaybackListener { void onStart(User user, Song song); }

/**
 * One user's player. Not shared between users, but a phone and a web tab could send
 * commands at once, so every public method is synchronized.
 */
final class MusicPlayer {
    private final User user;
    private final InstantSource clock;
    private final List<PlaybackListener> listeners = new ArrayList<>();
    private final Deque<Instant> recentSkips = new ArrayDeque<>();
    private List<Song> queue = List.of();
    private List<Integer> order = List.of();
    private int cursor;                         // index into order
    private int positionSec;
    private int songsSinceAd;
    private PlayerState state = PlayerState.STOPPED;
    private RepeatMode repeat = RepeatMode.OFF;
    private PlaybackOrder playbackOrder = new SequentialOrder();

    MusicPlayer(User user, InstantSource clock) { this.user = user; this.clock = clock; }

    synchronized void addListener(PlaybackListener l) { listeners.add(l); }

    synchronized void load(List<Song> songs) {
        queue = List.copyOf(songs);
        order = playbackOrder.order(queue.size(), 0);
        cursor = 0;
        state = PlayerState.STOPPED;
    }

    synchronized void play() {
        if (queue.isEmpty()) throw new IllegalStateException("queue is empty");
        start();
    }

    synchronized void pause() { if (state == PlayerState.PLAYING) state = PlayerState.PAUSED; }

    synchronized void resume() { if (state == PlayerState.PAUSED) state = PlayerState.PLAYING; }

    synchronized void seek(int sec) {
        positionSec = Math.max(0, Math.min(sec, current().durationSec()));
    }

    /** User pressed "next". Free users have a skip limit per rolling hour. */
    synchronized boolean skip() {
        if (state == PlayerState.STOPPED) return false;          // nothing to skip
        Instant now = clock.instant();
        while (!recentSkips.isEmpty() && recentSkips.peekFirst().isBefore(now.minus(Duration.ofHours(1)))) recentSkips.pollFirst();
        if (recentSkips.size() >= user.plan().skipsPerHour) {
            System.out.println("  [player] skip limit reached on " + user.plan() + " plan");
            return false;
        }
        recentSkips.addLast(now);
        advance(false);
        return true;
    }

    /** The current song ended naturally. Repeat-one replays it; skip does not honour repeat-one. */
    synchronized void trackFinished() { advance(true); }

    synchronized void previous() {
        if (positionSec > 3 || cursor == 0) { positionSec = 0; return; }   // like most players: restart first
        cursor--;
        start();
    }

    synchronized void setShuffle(boolean on) {
        int now = order.isEmpty() ? 0 : order.get(cursor);
        playbackOrder = on ? new ShuffleOrder(42) : new SequentialOrder();
        order = playbackOrder.order(queue.size(), now);
        cursor = 0;
    }

    synchronized void setRepeat(RepeatMode m) { repeat = m; }

    synchronized Song current() { return queue.get(order.get(cursor)); }
    synchronized PlayerState state() { return state; }
    synchronized int position() { return positionSec; }

    /** The next few songs in play order (what the "Up next" panel shows). */
    synchronized List<Song> upNext(int n) {
        List<Song> out = new ArrayList<>();
        for (int i = cursor + 1; i < order.size() && out.size() < n; i++) out.add(queue.get(order.get(i)));
        return out;
    }

    private void advance(boolean natural) {
        if (natural && repeat == RepeatMode.ONE) { start(); return; }
        if (cursor + 1 < order.size()) { cursor++; start(); return; }
        if (repeat == RepeatMode.ALL || repeat == RepeatMode.ONE) {   // wrap around (reshuffle if shuffling)
            order = playbackOrder.order(queue.size(), order.get(0));
            cursor = 0;
            start();
            return;
        }
        state = PlayerState.STOPPED;
        System.out.println("  [player] end of queue");
    }

    private void start() {
        if (user.plan().ads && ++songsSinceAd > 3) {
            System.out.println("  [player] (30-second ad for FREE plan)");
            songsSinceAd = 1;
        }
        positionSec = 0;
        state = PlayerState.PLAYING;
        Song s = current();
        System.out.println("  [player] > " + s);
        listeners.forEach(l -> l.onStart(user, s));
    }
}

/** Strategy for "what should this user hear next?" - a simple stub based on favourite genre. */
interface Recommender { List<Song> recommend(User user, int n); }

final class GenreRecommender implements Recommender {
    private final MusicLibrary library;
    GenreRecommender(MusicLibrary library) { this.library = library; }

    @Override public List<Song> recommend(User user, int n) {
        Map<Genre, Integer> counts = new EnumMap<>(Genre.class);
        user.history().forEach(s -> counts.merge(s.genre(), 1, Integer::sum));
        List<Genre> favourite = counts.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue()).map(Map.Entry::getKey).toList();
        List<Song> heard = user.history();
        List<Song> out = new ArrayList<>();
        for (Genre g : favourite)
            for (Song s : library.allSongs())
                if (out.size() < n && s.genre() == g && !heard.contains(s)) out.add(s);
        return out;
    }
}
