package lld.ch18.cricinfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

enum MatchFormat { ODI, T20, TEST }

enum MatchStatus { SCHEDULED, LIVE, FINISHED, DRAWN, CANCELED }

record Stadium(String name, String city, int capacity) {}

/** Outcome of a finished match. A sealed hierarchy lets callers switch over it exhaustively. */
sealed interface Result {
    record Win(Team winner, String margin) implements Result {
        @Override public String toString() { return winner + " won by " + margin; }
    }
    record Tie() implements Result {
        @Override public String toString() { return "Match tied"; }
    }
    record Draw() implements Result {
        @Override public String toString() { return "Match drawn"; }
    }
}

/**
 * Everything that is the same in every format lives here. The formats only
 * answer two questions: how many overs per innings, how many innings per side.
 * Observers (live scorecard, commentary feed, push alerts) subscribe to events.
 */
abstract class Match {
    private final Playing11 teamA;
    private final Playing11 teamB;
    private final Stadium stadium;
    private final LocalDate date;
    private final List<MatchOfficial> officials = new ArrayList<>();
    private final List<Innings> innings = new ArrayList<>();
    private final List<MatchObserver> observers = new CopyOnWriteArrayList<>();
    private MatchStatus status = MatchStatus.SCHEDULED;
    private Result result;

    Match(Playing11 teamA, Playing11 teamB, Stadium stadium, LocalDate date) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.stadium = stadium;
        this.date = date;
    }

    abstract MatchFormat format();
    abstract int oversPerInnings();       // 0 means no limit
    abstract int inningsPerSide();
    /** ODIs allow 10 overs per bowler and T20s 4, i.e. one fifth of the innings. */
    int maxOversPerBowler() { return oversPerInnings() / 5; }

    Playing11 teamA() { return teamA; }
    Playing11 teamB() { return teamB; }
    Stadium stadium() { return stadium; }
    LocalDate date() { return date; }
    synchronized MatchStatus status() { return status; }
    synchronized Optional<Result> result() { return Optional.ofNullable(result); }
    synchronized List<Innings> innings() { return List.copyOf(innings); }

    void addObserver(MatchObserver o) { observers.add(o); }

    void assign(MatchOfficial official) {
        official.assign(this);
        officials.add(official);
    }

    synchronized Innings startInnings(Playing11 batting) {
        if (status == MatchStatus.FINISHED || status == MatchStatus.DRAWN) throw new IllegalStateException("match is over");
        Innings current = currentInnings();
        if (current != null && !current.isComplete()) throw new IllegalStateException("current innings still in progress");
        if (innings.size() == 2 * inningsPerSide()) throw new IllegalStateException("all innings played");
        Playing11 bowling = batting == teamA ? teamB : teamA;
        boolean finalInnings = innings.size() == 2 * inningsPerSide() - 1;
        Integer target = finalInnings ? totalFor(bowling.team()) - totalFor(batting.team()) + 1 : null;
        Innings in = new Innings(innings.size() + 1, batting, bowling, oversPerInnings(), maxOversPerBowler(), target);
        innings.add(in);
        status = MatchStatus.LIVE;
        return in;
    }

    synchronized Over startOver(Player bowler) { return requireCurrent().startOver(bowler); }

    /** Records one delivery and fires the events; thread-safe so two scorers cannot interleave balls. */
    synchronized Ball bowl(BallOutcome outcome) {
        Innings in = requireCurrent();
        Ball ball = in.record(outcome);
        observers.forEach(o -> o.onBall(this, in, ball));
        Over over = in.overs().get(in.overs().size() - 1);
        if (over.isComplete() || in.isComplete()) observers.forEach(o -> o.onOverEnd(this, in, over));
        if (in.isComplete()) {
            observers.forEach(o -> o.onInningsEnd(this, in));
            boolean chased = in.target().map(t -> in.runs() >= t).orElse(false);
            if (innings.size() == 2 * inningsPerSide() || chased) finish(in);
        }
        return ball;
    }

    synchronized void declareDraw() {
        status = MatchStatus.DRAWN;
        result = new Result.Draw();
        observers.forEach(o -> o.onMatchEnd(this, result));
    }

    private void finish(Innings last) {
        Team chasing = last.battingTeam();
        Team other = last.bowlingTeam();
        int chasingRuns = totalFor(chasing);
        int otherRuns = totalFor(other);
        if (chasingRuns > otherRuns) result = new Result.Win(chasing, last.wicketsInHand() + " wickets");
        else if (chasingRuns < otherRuns) result = new Result.Win(other, (otherRuns - chasingRuns) + " runs");
        else result = new Result.Tie();
        status = MatchStatus.FINISHED;
        observers.forEach(o -> o.onMatchEnd(this, result));
    }

    private int totalFor(Team t) {
        return innings.stream().filter(i -> i.battingTeam() == t).mapToInt(Innings::runs).sum();
    }

    private Innings currentInnings() { return innings.isEmpty() ? null : innings.get(innings.size() - 1); }

    private Innings requireCurrent() {
        Innings in = currentInnings();
        if (in == null) throw new IllegalStateException("no innings started");
        return in;
    }

    @Override public String toString() { return format() + " " + teamA.team() + " v " + teamB.team() + " at " + stadium.name(); }
}

final class ODI extends Match {
    ODI(Playing11 a, Playing11 b, Stadium s, LocalDate d) { super(a, b, s, d); }
    @Override MatchFormat format() { return MatchFormat.ODI; }
    @Override int oversPerInnings() { return 50; }
    @Override int inningsPerSide() { return 1; }
}

final class T20 extends Match {
    T20(Playing11 a, Playing11 b, Stadium s, LocalDate d) { super(a, b, s, d); }
    @Override MatchFormat format() { return MatchFormat.T20; }
    @Override int oversPerInnings() { return 20; }
    @Override int inningsPerSide() { return 1; }
}

final class Test extends Match {
    Test(Playing11 a, Playing11 b, Stadium s, LocalDate d) { super(a, b, s, d); }
    @Override MatchFormat format() { return MatchFormat.TEST; }
    @Override int oversPerInnings() { return 0; }    // no limit
    @Override int inningsPerSide() { return 2; }
}
