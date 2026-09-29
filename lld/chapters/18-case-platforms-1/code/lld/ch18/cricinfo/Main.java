package lld.ch18.cricinfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // ---- teams, squads, tournament ----
        Team lions = team("Lions", "Arun", "Bala", "Chetan", "Dev", "Eshan", "Farhan", "Gopal", "Hari", "Imran", "Jai", "Kiran", "Lokesh", "Manav");
        Team tigers = team("Tigers", "Nabil", "Omar", "Pranav", "Qasim", "Rahul", "Sameer", "Tarun", "Uday", "Varun", "Wasim", "Yash", "Zubin", "Ajay");
        Tournament cup = new Tournament("Demo Cup");
        TournamentSquad lionsSquad = cup.announceSquad(lions);
        TournamentSquad tigersSquad = cup.announceSquad(tigers);
        lions.players().stream().limit(12).forEach(lionsSquad::add);      // Manav is not in the squad
        tigers.players().stream().limit(12).forEach(tigersSquad::add);

        System.out.println("== Picking the elevens ==");
        Playing11 lionsXI = new Playing11(lionsSquad);
        Playing11 tigersXI = new Playing11(tigersSquad);
        lions.players().stream().limit(11).forEach(lionsXI::add);
        tigers.players().stream().limit(11).forEach(tigersXI::add);
        attempt(() -> new Playing11(lionsSquad).add(find(lions, "Manav")));
        attempt(() -> lionsXI.add(find(lions, "Lokesh")));

        // ---- the match ----
        Stadium stadium = new Stadium("Riverside Oval", "Pune", 40000);
        T20 match = new T20(lionsXI, tigersXI, stadium, LocalDate.of(2026, 9, 28));
        cup.addMatch(match);
        Commentator ravi = new Commentator(new Person("Ravi", "IN"));
        match.assign(new Umpire(new Person("Ken", "NZ"), UmpireType.FIELD));
        match.assign(new Umpire(new Person("Paul", "AU"), UmpireType.FIELD));
        match.assign(new Referee(new Person("Chris", "UK")));
        match.assign(ravi);
        match.addObserver(new CommentaryFeed(ravi, 1));
        match.addObserver(new LiveScorecard(5));

        System.out.println("\n== " + match + " ==");
        match.startInnings(lionsXI);
        List<Player> tigersBowlers = bowlers(tigersXI);
        match.startOver(tigersBowlers.get(0));
        match.bowl(BallOutcome.four("driven through cover"));
        match.bowl(BallOutcome.wide("sprayed down leg"));
        match.bowl(BallOutcome.runs(1, "pushed to mid-on"));
        match.bowl(BallOutcome.noBall(0, "overstepped, free hit next"));
        match.bowl(BallOutcome.out(WicketType.BOWLED, "through the gate!"));
        match.bowl(BallOutcome.runOut(1, false, "a second run was never there"));
        match.bowl(BallOutcome.dot("defended"));
        match.bowl(BallOutcome.six("launched over long-on"));
        attempt(() -> match.startOver(tigersBowlers.get(0)));             // same bowler twice in a row
        Random rnd = new Random(23);
        simulate(match, rnd, tigersBowlers);

        match.startInnings(tigersXI);
        simulate(match, rnd, bowlers(lionsXI));

        System.out.println("\n== Scorecards ==");
        match.innings().forEach(Scorecard::print);
        cup.recordResult(match);
        System.out.println("Points table: " + cup.pointsTable());

        // ---- a second match (ODI) so the global stats have more data ----
        ODI odi = new ODI(tigersXI, lionsXI, stadium, LocalDate.of(2026, 10, 2));
        odi.addObserver(new LiveScorecard(25));
        System.out.println("\n== " + odi + " ==");
        odi.startInnings(tigersXI);
        simulate(odi, rnd, bowlers(lionsXI));
        odi.startInnings(lionsXI);
        simulate(odi, rnd, bowlers(tigersXI));

        // ---- stats, derived from balls ----
        StatsService statsService = new StatsService();
        List<Match> all = List.of(match, odi);
        Map<Player, PlayerStats> career = statsService.compute(all, null);
        System.out.println("\n== Global stat queries (all formats) ==");
        for (StatQuery q : StatQueries.ALL) {
            System.out.println("  " + q.question() + ": " + q.answer(career)
                    .map(l -> l.player() + " (" + l.value() + ")").orElse("nobody yet"));
        }
        Map<Player, PlayerStats> odiOnly = statsService.compute(all, MatchFormat.ODI);
        System.out.println("  Most runs in ODIs: " + new MostRuns().answer(odiOnly).map(l -> l.player() + " (" + l.value() + ")").orElse("-"));
        System.out.println("  Ravi is assigned to " + ravi.assignedMatches().size() + " match(es)");
    }

    /** Plays the rest of the current innings with random but legal deliveries. */
    static void simulate(Match m, Random rnd, List<Player> bowlers) {
        Innings in = m.innings().get(m.innings().size() - 1);
        int over = in.overs().size();
        while (!in.isComplete()) {
            m.startOver(bowlers.get(over % bowlers.size()));
            over++;
            while (!in.isComplete() && in.overs().get(in.overs().size() - 1).legalBalls() < Over.LEGAL_BALLS) {
                m.bowl(randomBall(rnd));
            }
        }
    }

    static BallOutcome randomBall(Random rnd) {
        int r = rnd.nextInt(1000);
        if (r < 330) return BallOutcome.dot(null);
        if (r < 640) return BallOutcome.runs(1, null);
        if (r < 720) return BallOutcome.runs(2, null);
        if (r < 730) return BallOutcome.runs(3, null);
        if (r < 810) return BallOutcome.four(null);                     // about 1 ball in 12 is a four
        if (r < 840) return BallOutcome.six(null);                      // and 1 in 33 a six: roughly 6.5 runs an over
        if (r < 870) return BallOutcome.wide(null);
        if (r < 880) return BallOutcome.noBall(0, null);
        if (r < 895) return BallOutcome.legBye(1, null);
        if (r < 905) return BallOutcome.out(WicketType.BOWLED, null);
        if (r < 920) return BallOutcome.out(WicketType.CAUGHT, null);
        if (r < 927) return BallOutcome.out(WicketType.LBW, null);
        if (r < 930) return BallOutcome.runOut(0, rnd.nextBoolean(), null);
        return BallOutcome.dot(null);
    }

    static List<Player> bowlers(Playing11 xi) {
        List<Player> order = xi.battingOrder();
        return new ArrayList<>(order.subList(order.size() - 5, order.size()));   // the last five are the bowlers
    }

    static Team team(String name, String... players) {
        Team t = new Team(name);
        for (String p : players) t.addPlayer(new Player(new Person(p, "IN")));
        return t;
    }

    static Player find(Team t, String name) {
        return t.players().stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
    }

    static void attempt(Runnable r) {
        try { r.run(); } catch (RuntimeException e) { System.out.println("  REJECTED: " + e.getMessage()); }
    }
}
