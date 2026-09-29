package lld.ch21.codingblox;

public class User {
    public static final int DEFAULT_SCORE = 1500;
    private final String name;
    private int score = DEFAULT_SCORE;

    public User(String name) { this.name = name; }
    public String name() { return name; }
    public synchronized int score() { return score; }
    /** Read-modify-write under one lock, so two contests finishing together cannot lose an update. */
    synchronized void applyContest(Level level, int points) { score = level.newScore(score, points); }
}
