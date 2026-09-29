package lld.ch18.cricinfo;

import java.util.ArrayList;
import java.util.List;

/** A human being. Roles (player, umpire, commentator) HOLD a person instead of extending it. */
record Person(String name, String country) {}

final class Player {
    private final Person person;
    Player(Person person) { this.person = person; }
    String name() { return person.name(); }
    Person person() { return person; }
    @Override public String toString() { return name(); }
}

enum UmpireType { FIELD, RESERVED, TV }

/** Umpires, referees and commentators share one behaviour: being assigned to matches. */
abstract class MatchOfficial {
    private final Person person;
    private final List<Match> assignedMatches = new ArrayList<>();

    MatchOfficial(Person person) { this.person = person; }

    String name() { return person.name(); }
    synchronized boolean assign(Match m) { return !assignedMatches.contains(m) && assignedMatches.add(m); }
    synchronized List<Match> assignedMatches() { return List.copyOf(assignedMatches); }
    abstract String role();
    @Override public String toString() { return role() + " " + name(); }
}

final class Umpire extends MatchOfficial {
    private final UmpireType type;
    Umpire(Person p, UmpireType type) { super(p); this.type = type; }
    @Override String role() { return type + " umpire"; }
}

final class Referee extends MatchOfficial {
    Referee(Person p) { super(p); }
    @Override String role() { return "referee"; }
}

final class Commentator extends MatchOfficial {
    Commentator(Person p) { super(p); }
    @Override String role() { return "commentator"; }
}
