package lld.ch18.cricinfo;

import java.util.ArrayList;
import java.util.List;

enum Extra { NONE, WIDE, NO_BALL, BYE, LEG_BYE }

enum WicketType {
    BOWLED, CAUGHT, LBW, STUMPED, HIT_WICKET, RUN_OUT, OBSTRUCTING_THE_FIELD;

    /** A run out (or obstructing the field) is a wicket for the fielding side, NOT for the bowler. */
    boolean creditedToBowler() { return this != RUN_OUT && this != OBSTRUCTING_THE_FIELD; }
}

/**
 * What happened on one delivery, as typed by the scorer. Who bowled and who
 * faced is filled in by the Innings, which knows the state of play.
 *
 * @param batRuns    runs off the bat (credited to the batter)
 * @param extra      kind of extra, if any
 * @param extraRuns  runs credited to the team as extras (a wide or no ball is at least 1)
 * @param boundary   true when batRuns came from a 4 or 6 hit to the rope (batters do not cross)
 * @param wicket     type of dismissal, or null
 * @param nonStrikerOut for a run out: true when the batter at the bowler's end is out
 */
record BallOutcome(int batRuns, Extra extra, int extraRuns, boolean boundary,
                   WicketType wicket, boolean nonStrikerOut, String commentary) {

    BallOutcome {
        if (batRuns < 0 || extraRuns < 0) throw new IllegalArgumentException("runs cannot be negative");
        if ((extra == Extra.WIDE || extra == Extra.NO_BALL) && extraRuns < 1)
            throw new IllegalArgumentException(extra + " carries at least one penalty run");
        // Laws of cricket: off a no ball the bowler cannot take a wicket; off a wide only stumped/hit wicket/run out.
        if (wicket != null && extra == Extra.NO_BALL && wicket.creditedToBowler())
            throw new IllegalArgumentException(wicket + " is not possible off a no ball");
        if (wicket != null && extra == Extra.WIDE && !(wicket == WicketType.STUMPED || wicket == WicketType.HIT_WICKET
                || !wicket.creditedToBowler()))
            throw new IllegalArgumentException(wicket + " is not possible off a wide");
    }

    static BallOutcome dot(String c) { return new BallOutcome(0, Extra.NONE, 0, false, null, false, c); }
    static BallOutcome runs(int n, String c) { return new BallOutcome(n, Extra.NONE, 0, false, null, false, c); }
    static BallOutcome four(String c) { return new BallOutcome(4, Extra.NONE, 0, true, null, false, c); }
    static BallOutcome six(String c) { return new BallOutcome(6, Extra.NONE, 0, true, null, false, c); }
    static BallOutcome wide(String c) { return new BallOutcome(0, Extra.WIDE, 1, false, null, false, c); }
    static BallOutcome noBall(int batRuns, String c) { return new BallOutcome(batRuns, Extra.NO_BALL, 1, batRuns >= 4, null, false, c); }
    static BallOutcome legBye(int n, String c) { return new BallOutcome(0, Extra.LEG_BYE, n, false, null, false, c); }
    static BallOutcome bye(int n, String c) { return new BallOutcome(0, Extra.BYE, n, false, null, false, c); }
    static BallOutcome out(WicketType w, String c) { return new BallOutcome(0, Extra.NONE, 0, false, w, false, c); }
    static BallOutcome runOut(int completedRuns, boolean nonStrikerOut, String c) {
        return new BallOutcome(completedRuns, Extra.NONE, 0, false, WicketType.RUN_OUT, nonStrikerOut, c);
    }

    /** A wide or a no ball is not one of the six balls of an over. */
    boolean isLegal() { return extra != Extra.WIDE && extra != Extra.NO_BALL; }
    boolean countsAsBallFaced() { return extra != Extra.WIDE; }
    int totalRuns() { return batRuns + extraRuns; }
    /** Byes and leg byes are not the bowler's fault; wides and no balls are. */
    int runsChargedToBowler() { return batRuns + (isLegal() ? 0 : extraRuns); }

    /** Runs physically run between the wickets: an odd number means the batters swapped ends. */
    int runsRun() {
        if (boundary) return 0;
        return switch (extra) {
            case NONE -> batRuns;
            case WIDE -> extraRuns - 1;
            case NO_BALL -> batRuns;
            case BYE, LEG_BYE -> extraRuns;
        };
    }
}

record Wicket(WicketType type, Player batter, Player bowler) {
    boolean creditedToBowler() { return type.creditedToBowler(); }
}

/** One delivery as recorded: owns its outcome, its wicket and its commentary (composition). */
record Ball(int overNumber, int deliveryInOver, Player bowler, Player striker, BallOutcome outcome, Wicket wicket) {
    /** Scorecard style: over 1, ball 3 is "0.3" (completed overs . ball); an extra shows the number of the ball it replaces. */
    String label() { return (overNumber - 1) + "." + deliveryInOver; }
}

/** An over is SIX LEGAL deliveries, not six balls. */
final class Over {
    static final int LEGAL_BALLS = 6;
    private final int number;
    private final Player bowler;
    private final List<Ball> balls = new ArrayList<>();

    Over(int number, Player bowler) { this.number = number; this.bowler = bowler; }

    int number() { return number; }
    Player bowler() { return bowler; }
    void add(Ball b) {
        if (isComplete()) throw new IllegalStateException("over " + number + " is complete");
        balls.add(b);
    }
    List<Ball> balls() { return List.copyOf(balls); }
    int legalBalls() { return (int) balls.stream().filter(b -> b.outcome().isLegal()).count(); }
    boolean isComplete() { return legalBalls() == LEGAL_BALLS; }
    int runsConceded() { return balls.stream().mapToInt(b -> b.outcome().runsChargedToBowler()).sum(); }
    boolean isMaiden() { return isComplete() && runsConceded() == 0; }
}
