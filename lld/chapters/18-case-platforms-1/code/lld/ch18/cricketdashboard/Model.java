package lld.ch18.cricketdashboard;

import java.util.ArrayList;
import java.util.List;

/** One input token, parsed once. Factory method: BallEvent.parse("Wd") etc. */
sealed interface BallEvent {
    record Runs(int runs) implements BallEvent {}
    record Wide() implements BallEvent {}
    record NoBall(int batRuns) implements BallEvent {}
    record Wicket() implements BallEvent {}

    static BallEvent parse(String token) {
        String t = token.trim();
        if (t.equalsIgnoreCase("W")) return new Wicket();
        if (t.equalsIgnoreCase("Wd")) return new Wide();
        if (t.regionMatches(true, 0, "Nb", 0, 2)) return new NoBall(t.length() > 2 ? Integer.parseInt(t.substring(2)) : 0);
        int runs = Integer.parseInt(t);
        if (runs < 0 || runs > 6) throw new IllegalArgumentException("bad runs: " + t);
        return new Runs(runs);
    }

    default boolean isLegal() { return !(this instanceof Wide || this instanceof NoBall); }
}

/** A batter's line on the scorecard. */
final class Batter {
    final String name;
    int runs, balls, fours, sixes;
    boolean out;

    Batter(String name) { this.name = name; }

    void faced(int batRuns) {
        runs += batRuns;
        balls++;
        if (batRuns == 4) fours++;
        if (batRuns == 6) sixes++;
    }

    String strikeRate() { return balls == 0 ? "-" : String.format("%.1f", 100.0 * runs / balls); }
}

/** One team's batting innings: owns strike rotation, extras, and the "is it over?" rule. */
final class TeamInnings {
    private final String team;
    private final List<Batter> order = new ArrayList<>();
    private final int maxOvers;
    private final Integer target;           // null for the first innings
    private int striker = 0, nonStriker = 1, next = 2;
    private int total, wickets, legalBalls, extras;

    TeamInnings(String team, List<String> battingOrder, int maxOvers, Integer target) {
        if (battingOrder.size() < 2) throw new IllegalArgumentException("need at least two batters");
        this.team = team;
        battingOrder.forEach(n -> order.add(new Batter(n)));
        this.maxOvers = maxOvers;
        this.target = target;
    }

    void play(BallEvent e) {
        if (isOver()) throw new IllegalStateException("innings already finished");
        Batter s = order.get(striker);
        switch (e) {
            case BallEvent.Wide w -> { total += 1; extras += 1; }
            case BallEvent.NoBall nb -> {
                total += 1 + nb.batRuns(); extras += 1;
                s.faced(nb.batRuns());               // a no ball counts as a ball faced by the batter
                if (nb.batRuns() % 2 == 1) swap();
            }
            case BallEvent.Wicket w -> {
                legalBalls++;
                s.balls++;
                s.out = true;
                wickets++;
                if (!allOut()) striker = next++;     // new batter takes strike
            }
            case BallEvent.Runs r -> {
                legalBalls++;
                total += r.runs();
                s.faced(r.runs());
                if (r.runs() % 2 == 1) swap();       // 1s and 3s change strike
            }
        }
        if (e.isLegal() && legalBalls % 6 == 0 && !isOver()) swap();  // end of over
    }

    boolean isOver() {
        return allOut() || legalBalls == maxOvers * 6 || (target != null && total >= target);
    }

    private boolean allOut() { return wickets == order.size() - 1; }
    private void swap() { int t = striker; striker = nonStriker; nonStriker = t; }

    String team() { return team; }
    int total() { return total; }
    int wickets() { return wickets; }
    int legalBalls() { return legalBalls; }
    int extras() { return extras; }
    int wicketsInHand() { return order.size() - 1 - wickets; }
    List<Batter> batters() { return List.copyOf(order); }
    boolean isAtCrease(Batter b) { return !b.out && (order.get(striker) == b || order.get(nonStriker) == b); }

    String overs() { return legalBalls / 6 + (legalBalls % 6 == 0 ? "" : "." + legalBalls % 6); }
}
