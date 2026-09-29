package lld.ch18.cricinfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Everyone the team has ever picked. */
final class Team {
    private final String name;
    private final Set<Player> players = new LinkedHashSet<>();

    Team(String name) { this.name = name; }
    String name() { return name; }
    void addPlayer(Player p) { players.add(p); }
    List<Player> players() { return List.copyOf(players); }   // keeps insertion order
    @Override public String toString() { return name; }
}

/** Who a team may use in ONE tournament: a subset of the team. */
final class TournamentSquad {
    private final Team team;
    private final Set<Player> players = new LinkedHashSet<>();

    TournamentSquad(Team team) { this.team = team; }
    Team team() { return team; }
    void add(Player p) {
        if (!team.players().contains(p)) throw new IllegalArgumentException(p + " does not play for " + team);
        players.add(p);
    }
    boolean contains(Player p) { return players.contains(p); }
}

/** Who takes the field in ONE match: exactly eleven, all from the squad. The class enforces the rule. */
final class Playing11 {
    static final int SIZE = 11;
    private final TournamentSquad squad;
    private final List<Player> players = new ArrayList<>();   // batting order

    Playing11(TournamentSquad squad) { this.squad = squad; }

    void add(Player p) {
        if (!squad.contains(p)) throw new IllegalArgumentException(p + " is not in the " + squad.team() + " squad");
        if (players.size() == SIZE) throw new IllegalStateException("a playing eleven cannot have a twelfth player (" + p + ")");
        if (players.contains(p)) throw new IllegalArgumentException(p + " already picked");
        players.add(p);
    }

    Team team() { return squad.team(); }
    List<Player> battingOrder() { return List.copyOf(players); }
    boolean isComplete() { return players.size() == SIZE; }
    boolean contains(Player p) { return players.contains(p); }
}

/** A tournament owns its squads and keeps a points table (2 for a win, 1 each for a tie or no result). */
final class Tournament {
    private final String name;
    private final Map<Team, TournamentSquad> squads = new LinkedHashMap<>();
    private final Map<Team, Integer> points = new LinkedHashMap<>();
    private final List<Match> matches = new ArrayList<>();

    Tournament(String name) { this.name = name; }
    String name() { return name; }

    TournamentSquad announceSquad(Team team) {
        TournamentSquad s = new TournamentSquad(team);
        squads.put(team, s);
        points.putIfAbsent(team, 0);
        return s;
    }

    void addMatch(Match m) { matches.add(m); }
    List<Match> matches() { return List.copyOf(matches); }

    void recordResult(Match m) {
        Result r = m.result().orElseThrow(() -> new IllegalStateException("match not finished"));
        switch (r) {
            case Result.Win w -> points.merge(w.winner(), 2, Integer::sum);
            case Result.Tie t -> { points.merge(m.teamA().team(), 1, Integer::sum); points.merge(m.teamB().team(), 1, Integer::sum); }
            case Result.Draw d -> { }                     // Tests are not part of a points table here
        }
    }

    List<Map.Entry<Team, Integer>> pointsTable() {
        return points.entrySet().stream()
                .sorted(Map.Entry.<Team, Integer>comparingByValue(Comparator.reverseOrder()))
                .toList();
    }
}
