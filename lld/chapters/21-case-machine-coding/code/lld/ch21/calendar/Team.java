package lld.ch21.calendar;

import java.util.List;

/** Members are kept in onboarding order; representative selection walks this order. */
public record Team(String name, List<User> members) {
    public Team { members = List.copyOf(members); }
}
