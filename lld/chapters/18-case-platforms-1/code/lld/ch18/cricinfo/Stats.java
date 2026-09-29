package lld.ch18.cricinfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Career numbers for one player, always DERIVED from balls, never typed in by hand. */
final class PlayerStats {
    final Player player;
    int runs, ballsFaced, fours, sixes, fifties, hundreds, highest;
    int wickets, ballsBowled, runsConceded;

    PlayerStats(Player player) { this.player = player; }

    /** Per-ball numbers. */
    void countBall(Ball b, boolean asBatter) {
        BallOutcome o = b.outcome();
        if (asBatter) {
            runs += o.batRuns();
            if (o.countsAsBallFaced()) ballsFaced++;
            if (o.boundary() && o.batRuns() == 4) fours++;
            if (o.boundary() && o.batRuns() == 6) sixes++;
        } else {
            if (o.isLegal()) ballsBowled++;
            runsConceded += o.runsChargedToBowler();
            if (b.wicket() != null && b.wicket().creditedToBowler()) wickets++;
        }
    }

    /** Milestones are per innings, so they are counted once the innings score is known. */
    void countInnings(int inningsRuns) {
        if (inningsRuns >= 100) hundreds++;
        else if (inningsRuns >= 50) fifties++;
        highest = Math.max(highest, inningsRuns);
    }
}

/** Recomputes stats from the recorded balls of the given matches, optionally for one format. */
final class StatsService {
    Map<Player, PlayerStats> compute(Collection<Match> matches, MatchFormat formatOrNull) {
        Map<Player, PlayerStats> stats = new HashMap<>();
        for (Match m : matches) {
            if (formatOrNull != null && m.format() != formatOrNull) continue;
            for (Innings in : m.innings()) {
                Map<Player, Integer> inningsRuns = new HashMap<>();
                for (Ball b : in.balls()) {
                    stats.computeIfAbsent(b.striker(), PlayerStats::new).countBall(b, true);
                    stats.computeIfAbsent(b.bowler(), PlayerStats::new).countBall(b, false);
                    inningsRuns.merge(b.striker(), b.outcome().batRuns(), Integer::sum);
                }
                inningsRuns.forEach((p, r) -> stats.get(p).countInnings(r));
            }
        }
        return stats;
    }
}

/**
 * Template Method: answer() is the fixed algorithm (walk all players, keep the best);
 * each question fills in one step, valueOf(). A new statistic = one new subclass.
 */
abstract class StatQuery {
    record Leader(Player player, int value) {}

    abstract String question();
    abstract int valueOf(PlayerStats s);

    final Optional<Leader> answer(Map<Player, PlayerStats> stats) {
        Leader best = null;
        for (PlayerStats s : stats.values()) {
            int v = valueOf(s);
            if (v > 0 && (best == null || v > best.value()
                    || (v == best.value() && s.player.name().compareTo(best.player().name()) < 0))) {
                best = new Leader(s.player, v);
            }
        }
        return Optional.ofNullable(best);
    }
}

final class MostRuns extends StatQuery {
    @Override String question() { return "Most runs"; }
    @Override int valueOf(PlayerStats s) { return s.runs; }
}

final class MostHundreds extends StatQuery {
    @Override String question() { return "Most hundreds"; }
    @Override int valueOf(PlayerStats s) { return s.hundreds; }
}

final class MostWickets extends StatQuery {
    @Override String question() { return "Highest wicket taker"; }
    @Override int valueOf(PlayerStats s) { return s.wickets; }
}

final class MostSixes extends StatQuery {
    @Override String question() { return "Most sixes"; }
    @Override int valueOf(PlayerStats s) { return s.sixes; }
}

final class StatQueries {
    static final List<StatQuery> ALL = List.of(new MostRuns(), new MostHundreds(), new MostWickets(), new MostSixes());
    private StatQueries() {}
}
