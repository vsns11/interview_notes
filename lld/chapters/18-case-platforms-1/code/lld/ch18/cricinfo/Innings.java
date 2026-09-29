package lld.ch18.cricinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One side's turn to bat. The innings owns its overs, so it is the ONE place
 * that decides when an over ends, who is on strike, and when the innings is over.
 */
final class Innings {
    private final int number;
    private final Playing11 batting;
    private final Playing11 bowling;
    private final int maxOvers;              // 0 = no limit (Test)
    private final int maxOversPerBowler;     // 0 = no limit
    private final Integer target;            // runs needed to win, only for the final innings
    private final List<Over> overs = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final Map<Player, Integer> oversByBowler = new HashMap<>();
    private Player striker;
    private Player nonStriker;
    private int nextBatter = 2;
    private int runs;
    private int wickets;
    private boolean closed;

    Innings(int number, Playing11 batting, Playing11 bowling, int maxOvers, int maxOversPerBowler, Integer target) {
        if (!batting.isComplete() || !bowling.isComplete()) throw new IllegalStateException("both sides need eleven players");
        this.number = number;
        this.batting = batting;
        this.bowling = bowling;
        this.maxOvers = maxOvers;
        this.maxOversPerBowler = maxOversPerBowler;
        this.target = target;
        this.striker = batting.battingOrder().get(0);
        this.nonStriker = batting.battingOrder().get(1);
    }

    int number() { return number; }
    Team battingTeam() { return batting.team(); }
    Team bowlingTeam() { return bowling.team(); }
    Playing11 battingSide() { return batting; }
    int runs() { return runs; }
    int wickets() { return wickets; }
    Optional<Integer> target() { return Optional.ofNullable(target); }
    List<Ball> balls() { return List.copyOf(balls); }
    List<Over> overs() { return List.copyOf(overs); }
    Player striker() { return striker; }
    Player nonStriker() { return nonStriker; }

    Over startOver(Player bowler) {
        if (isComplete()) throw new IllegalStateException("innings is complete");
        Over current = currentOver();
        if (current != null && !current.isComplete()) throw new IllegalStateException("over " + current.number() + " is still in progress");
        if (!bowling.contains(bowler)) throw new IllegalArgumentException(bowler + " is not in the fielding eleven");
        if (current != null && current.bowler().equals(bowler))
            throw new IllegalStateException(bowler + " cannot bowl two overs in a row");
        int bowled = oversByBowler.getOrDefault(bowler, 0);
        if (maxOversPerBowler > 0 && bowled >= maxOversPerBowler)
            throw new IllegalStateException(bowler + " has used all " + maxOversPerBowler + " overs");
        oversByBowler.put(bowler, bowled + 1);
        Over over = new Over(overs.size() + 1, bowler);
        overs.add(over);
        return over;
    }

    Ball record(BallOutcome o) {
        Over over = currentOver();
        if (over == null || over.isComplete()) throw new IllegalStateException("start a new over first");
        if (isComplete()) throw new IllegalStateException("innings is complete");

        int deliveryNo = over.legalBalls() + 1;          // a wide or no ball is re-bowled, so it shares the next number
        Wicket wicket = null;
        if (o.wicket() != null) wicket = new Wicket(o.wicket(), o.nonStrikerOut() ? nonStriker : striker, over.bowler());
        Ball ball = new Ball(over.number(), deliveryNo, over.bowler(), striker, o, wicket);
        over.add(ball);
        balls.add(ball);
        runs += o.totalRuns();

        if (o.runsRun() % 2 == 1) swapEnds();              // odd runs: batters crossed
        if (wicket != null) {
            wickets++;
            if (!allOut()) {
                Player newBatter = batting.battingOrder().get(nextBatter++);
                if (wicket.batter().equals(striker)) striker = newBatter; else nonStriker = newBatter;
            }
        }
        if (over.isComplete()) swapEnds();                  // ends change after every over
        return ball;
    }

    boolean isComplete() {
        if (closed || allOut()) return true;
        if (target != null && runs >= target) return true;
        Over current = currentOver();
        return maxOvers > 0 && overs.size() == maxOvers && current != null && current.isComplete();
    }

    /** A Test captain may declare; the demo also uses it to stop an unlimited innings. */
    void declare() { closed = true; }

    int wicketsInHand() { return batting.battingOrder().size() - 1 - wickets; }

    /** Scorecard style: runs/wickets (completed overs.legal balls). */
    String score() {
        return battingTeam() + " " + runs + "/" + wickets + " (" + oversText() + " ov)";
    }

    String oversText() {
        Over current = currentOver();
        if (current == null) return "0";
        int full = current.isComplete() ? overs.size() : overs.size() - 1;
        int part = current.isComplete() ? 0 : current.legalBalls();
        return part == 0 ? String.valueOf(full) : full + "." + part;
    }

    private boolean allOut() { return wickets == batting.battingOrder().size() - 1; }
    private Over currentOver() { return overs.isEmpty() ? null : overs.get(overs.size() - 1); }
    private void swapEnds() { Player t = striker; striker = nonStriker; nonStriker = t; }
}
