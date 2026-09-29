package lld.ch18.stackoverflow;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A registered member. A guest has no Member object at all: every write
 * operation on the StackOverflow facade takes a Member, so "guests can only
 * search and view" is enforced by the method signatures.
 */
class Member {
    private static final AtomicLong IDS = new AtomicLong();

    private final long id = IDS.incrementAndGet();
    private final String name;
    private final String email;
    private final AtomicInteger reputation = new AtomicInteger(1);   // every account starts at 1
    private final Set<Badge> badges = Collections.synchronizedSet(new LinkedHashSet<>());
    private volatile AccountStatus status = AccountStatus.ACTIVE;

    Member(String name, String email) {
        this.name = name;
        this.email = email;
    }

    long id() { return id; }
    String name() { return name; }
    String email() { return email; }
    int reputation() { return reputation.get(); }
    AccountStatus status() { return status; }
    void setStatus(AccountStatus status) { this.status = status; }

    /** Applies a delta atomically; reputation never drops below 1 (the site's floor). Returns the new value. */
    int changeReputation(int delta) {
        return reputation.updateAndGet(r -> Math.max(1, r + delta));
    }

    /** Demo helper: pretend the member earned reputation before the demo started. */
    void seedReputation(int value) { reputation.set(value); }

    boolean hasPrivilege(Privilege p) { return reputation() >= p.minReputation; }

    boolean addBadge(Badge b) { return badges.add(b); }
    Set<Badge> badges() { synchronized (badges) { return Set.copyOf(badges); } }

    @Override public String toString() { return name + "(" + reputation() + ")"; }
}

/** Moderators can do everything a member can, whatever their reputation, plus close/reopen/delete/undelete any question. */
class Moderator extends Member {
    Moderator(String name, String email) { super(name, email); }
    @Override boolean hasPrivilege(Privilege p) { return true; }
}

/** Admins block and unblock members. */
class Admin extends Member {
    Admin(String name, String email) { super(name, email); }
    @Override boolean hasPrivilege(Privilege p) { return true; }
}
