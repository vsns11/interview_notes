package lld.ch18.cricinfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Observer: the match publishes events; screens, feeds and alerts subscribe. */
interface MatchObserver {
    default void onBall(Match m, Innings in, Ball b) {}
    default void onOverEnd(Match m, Innings in, Over o) {}
    default void onInningsEnd(Match m, Innings in) {}
    default void onMatchEnd(Match m, Result r) {}
}

/** Ball-by-ball commentary written by a commentator (prints only the first few overs of each innings in the demo). */
final class CommentaryFeed implements MatchObserver {
    private final Commentator commentator;
    private final int oversToShow;

    CommentaryFeed(Commentator commentator, int oversToShow) {
        this.commentator = commentator;
        this.oversToShow = oversToShow;
    }

    @Override public void onBall(Match m, Innings in, Ball b) {
        if (b.overNumber() > oversToShow) return;
        String what = describe(b);
        String text = b.outcome().commentary() == null ? "" : "  [" + commentator.name() + ": " + b.outcome().commentary() + "]";
        System.out.printf("  %-4s %s to %s, %s%s%n", b.label(), b.bowler(), b.striker(), what, text);
    }

    private static String describe(Ball b) {
        BallOutcome o = b.outcome();
        if (b.wicket() != null) return "OUT " + b.wicket().type() + " (" + b.wicket().batter() + ")"
                + (b.wicket().creditedToBowler() ? "" : " - not the bowler's wicket");
        return switch (o.extra()) {
            case WIDE -> "wide";
            case NO_BALL -> "no ball" + (o.batRuns() > 0 ? " + " + o.batRuns() : "");
            case BYE -> o.extraRuns() + " bye(s)";
            case LEG_BYE -> o.extraRuns() + " leg bye(s)";
            case NONE -> o.batRuns() == 0 ? "no run" : o.boundary() ? o.batRuns() == 4 ? "FOUR" : "SIX" : o.batRuns() + " run(s)";
        };
    }
}

/** Live score line at the end of an over (every n overs, to keep the demo short). */
final class LiveScorecard implements MatchObserver {
    private final int everyNOvers;
    LiveScorecard(int everyNOvers) { this.everyNOvers = everyNOvers; }

    @Override public void onOverEnd(Match m, Innings in, Over o) {
        if (o.number() % everyNOvers == 0 || in.isComplete()) {
            String chase = in.target().filter(t -> in.runs() < t).map(t -> "  need " + (t - in.runs()) + " to win").orElse("");
            System.out.println("  [live] " + in.score() + chase);
        }
    }

    @Override public void onInningsEnd(Match m, Innings in) {
        System.out.println("  [innings " + in.number() + " over] " + in.score());
    }

    @Override public void onMatchEnd(Match m, Result r) {
        System.out.println("  [result] " + r);
    }
}

/** Builds batting and bowling cards from the recorded balls (nothing is stored twice). */
final class Scorecard {
    static void print(Innings in) {
        System.out.println("Batting - " + in.battingTeam());
        Map<Player, int[]> bat = new LinkedHashMap<>();   // runs, balls, 4s, 6s
        Map<Player, String> howOut = new LinkedHashMap<>();
        for (Ball b : in.balls()) {
            int[] s = bat.computeIfAbsent(b.striker(), k -> new int[4]);
            BallOutcome o = b.outcome();
            s[0] += o.batRuns();
            if (o.countsAsBallFaced()) s[1]++;
            if (o.boundary() && o.batRuns() == 4) s[2]++;
            if (o.boundary() && o.batRuns() == 6) s[3]++;
            if (b.wicket() != null) {
                bat.computeIfAbsent(b.wicket().batter(), k -> new int[4]);
                howOut.put(b.wicket().batter(), b.wicket().creditedToBowler()
                        ? (b.wicket().type() == WicketType.BOWLED ? "" : b.wicket().type().name().toLowerCase().replace('_', ' ') + " ")
                                + "b " + b.bowler()
                        : "run out");
            }
        }
        bat.computeIfAbsent(in.striker(), k -> new int[4]);        // the two batters at the crease are listed
        bat.computeIfAbsent(in.nonStriker(), k -> new int[4]);     // even if one has not faced a ball yet
        List<Player> order = in.battingSide().battingOrder();
        for (Player p : order) {
            int[] s = bat.get(p);
            if (s == null) continue;
            System.out.printf("  %-10s %-22s %3d (%d)  4s:%d 6s:%d%n", p, howOut.getOrDefault(p, "not out"), s[0], s[1], s[2], s[3]);
        }
        int extras = in.balls().stream().mapToInt(b -> b.outcome().extraRuns()).sum();
        System.out.println("  Extras " + extras + "   Total " + in.runs() + "/" + in.wickets() + " (" + in.oversText() + " ov)");

        System.out.println("Bowling - " + in.bowlingTeam());
        Map<Player, int[]> bowl = new LinkedHashMap<>();  // legal balls, maidens, runs, wickets
        for (Over o : in.overs()) {
            int[] s = bowl.computeIfAbsent(o.bowler(), k -> new int[4]);
            s[0] += o.legalBalls();
            if (o.isMaiden()) s[1]++;
            s[2] += o.runsConceded();
            s[3] += (int) o.balls().stream().filter(b -> b.wicket() != null && b.wicket().creditedToBowler()).count();
        }
        bowl.forEach((p, s) -> System.out.printf("  %-10s %d.%d-%d-%d-%d%n", p, s[0] / 6, s[0] % 6, s[1], s[2], s[3]));
    }
}
