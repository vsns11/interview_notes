package lld.ch07.towardpatterns;

import java.util.Map;

/** Introduce Special Case (Null Object): stop repeating "if (customer == null)" at every call site. */
public final class SpecialCaseRefactoring {

    // ---------- BEFORE ----------
    record LegacyCustomer(String name, String plan) { }

    static final Map<String, LegacyCustomer> LEGACY_DB = Map.of("c1", new LegacyCustomer("Ravi", "premium"));

    static String legacyGreeting(String id) {
        LegacyCustomer c = LEGACY_DB.get(id);
        String name = (c == null) ? "occupant" : c.name();       // null check #1
        String plan = (c == null) ? "basic" : c.plan();          // null check #2 ... and so on in every client
        return "Hello " + name + " on " + plan;
    }

    // ---------- AFTER ----------
    sealed interface Customer permits KnownCustomer, UnknownCustomer {
        String name();
        String plan();
    }
    record KnownCustomer(String name, String plan) implements Customer { }
    /** The special case: one place holds the default behaviour. */
    record UnknownCustomer() implements Customer {
        @Override public String name() { return "occupant"; }
        @Override public String plan() { return "basic"; }
    }

    static final Map<String, Customer> DB = Map.of("c1", new KnownCustomer("Ravi", "premium"));

    static Customer find(String id) {
        return DB.getOrDefault(id, new UnknownCustomer());
    }

    static String greeting(String id) {
        Customer c = find(id);                                    // no null checks at all
        return "Hello " + c.name() + " on " + c.plan();
    }

    static boolean demo() {
        boolean same = legacyGreeting("c1").equals(greeting("c1")) && legacyGreeting("zz").equals(greeting("zz"));
        System.out.println("Special Case:    " + greeting("c1") + " / " + greeting("zz") + " | same: " + same);
        return same;
    }
}
