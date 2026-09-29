package lld.ch18.linkedin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

enum AccountStatus { ACTIVE, BLOCKED, BANNED, COMPROMISED, ARCHIVED, UNKNOWN }

class Member {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private volatile String name;
    private final String email;
    private final Profile profile = new Profile();
    private volatile AccountStatus status = AccountStatus.ACTIVE;
    // Connections change only through ConnectionService, which keeps both sides symmetric.
    private final Set<Long> connections = ConcurrentHashMap.newKeySet();
    // Following is one-sided: nobody has to agree, so it is just a set on the follower.
    private final Set<Long> followingMembers = ConcurrentHashMap.newKeySet();
    private final Set<Long> followingCompanies = ConcurrentHashMap.newKeySet();
    private final List<Post> posts = new CopyOnWriteArrayList<>();
    private final List<Message> inbox = new CopyOnWriteArrayList<>();
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    Member(String name, String email) { this.name = name; this.email = email; }

    long id() { return id; }
    String name() { return name; }
    void rename(String newName) { this.name = newName; }
    String email() { return email; }
    Profile profile() { return profile; }
    AccountStatus status() { return status; }
    void setStatus(AccountStatus s) { status = s; }

    Set<Long> connections() { return Set.copyOf(connections); }
    boolean isConnectedTo(Member other) { return connections.contains(other.id()); }
    void addConnection(long otherId) { connections.add(otherId); }         // package-private: only the service calls it
    void removeConnection(long otherId) { connections.remove(otherId); }

    boolean follow(Member m) { return m.id != id && followingMembers.add(m.id()); }
    boolean unfollow(Member m) { return followingMembers.remove(m.id()); }
    boolean followCompany(Company c) { return followingCompanies.add(c.id()); }
    Set<Long> followingMembers() { return Set.copyOf(followingMembers); }
    Set<Long> followingCompanies() { return Set.copyOf(followingCompanies); }

    void addPost(Post p) { posts.add(p); }
    List<Post> posts() { return List.copyOf(posts); }
    void receive(Message m) { inbox.add(m); }
    List<Message> inbox() { return List.copyOf(inbox); }
    void notify(Notification n) { notifications.add(n); }
    List<Notification> notifications() { return List.copyOf(notifications); }

    @Override public String toString() { return name; }
}

final class Admin extends Member {
    Admin(String name, String email) { super(name, email); }
}

enum InvitationStatus {
    PENDING, ACCEPTED, REJECTED, CANCELED;
    boolean canMoveTo(InvitationStatus next) { return this == PENDING && next != PENDING; }
}

/** A connection request is an object with a state over time: it can remember that it was rejected. */
final class ConnectionInvitation {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Member from;
    private final Member to;
    private final String note;
    private InvitationStatus status = InvitationStatus.PENDING;

    ConnectionInvitation(Member from, Member to, String note) { this.from = from; this.to = to; this.note = note; }

    long id() { return id; }
    Member from() { return from; }
    Member to() { return to; }
    String note() { return note; }
    synchronized InvitationStatus status() { return status; }

    synchronized void moveTo(InvitationStatus next) {
        if (!status.canMoveTo(next)) throw new IllegalStateException("invitation is already " + status);
        status = next;
    }
}

/**
 * Accepting an invitation changes TWO members, so it belongs to neither: it lives here.
 * This service is the only code that must be right for connections to stay symmetric.
 */
final class ConnectionService {
    private final Map<Long, ConnectionInvitation> invitations = new ConcurrentHashMap<>();
    private final NotificationService notifications;
    private final LongFunction<Member> lookup;

    ConnectionService(NotificationService notifications, LongFunction<Member> lookup) {
        this.notifications = notifications;
        this.lookup = lookup;
    }

    synchronized ConnectionInvitation invite(Member from, Member to, String note) {
        if (from.id() == to.id()) throw new IllegalArgumentException("cannot connect to yourself");
        if (from.isConnectedTo(to)) throw new IllegalStateException(from + " and " + to + " are already connected");
        if (pendingBetween(from, to)) throw new IllegalStateException("an invitation between " + from + " and " + to + " is already pending");
        ConnectionInvitation inv = new ConnectionInvitation(from, to, note);
        invitations.put(inv.id(), inv);
        notifications.send(to, NotificationType.CONNECTION_INVITATION, from + " wants to connect: \"" + note + "\"");
        return inv;
    }

    /** One lock for the whole graph change: both sides are updated together or not at all. */
    synchronized void accept(ConnectionInvitation inv, Member by) {
        if (by.id() != inv.to().id()) throw new IllegalStateException("only " + inv.to() + " can accept");
        inv.moveTo(InvitationStatus.ACCEPTED);
        inv.from().addConnection(inv.to().id());
        inv.to().addConnection(inv.from().id());
    }

    synchronized void reject(ConnectionInvitation inv, Member by) {
        if (by.id() != inv.to().id()) throw new IllegalStateException("only " + inv.to() + " can reject");
        inv.moveTo(InvitationStatus.REJECTED);
    }

    synchronized void withdraw(ConnectionInvitation inv, Member by) {
        if (by.id() != inv.from().id()) throw new IllegalStateException("only " + inv.from() + " can withdraw");
        inv.moveTo(InvitationStatus.CANCELED);
    }

    synchronized void removeConnection(Member a, Member b) {
        a.removeConnection(b.id());
        b.removeConnection(a.id());
    }

    boolean pendingBetween(Member a, Member b) {
        return invitations.values().stream().anyMatch(i -> i.status() == InvitationStatus.PENDING
                && ((i.from() == a && i.to() == b) || (i.from() == b && i.to() == a)));
    }

    /** Degree of connection (1st, 2nd, 3rd) by breadth-first search; empty means "out of network". */
    OptionalInt degree(Member from, Member to) {
        if (from.id() == to.id()) return OptionalInt.of(0);
        Set<Long> seen = new HashSet<>(Set.of(from.id()));
        Deque<long[]> queue = new ArrayDeque<>();                 // {memberId, depth}
        queue.add(new long[]{from.id(), 0});
        while (!queue.isEmpty()) {
            long[] cur = queue.poll();
            if (cur[1] == 3) continue;                              // LinkedIn stops at 3rd degree
            for (long next : lookup.apply(cur[0]).connections()) {
                if (next == to.id()) return OptionalInt.of((int) cur[1] + 1);
                if (seen.add(next)) queue.add(new long[]{next, cur[1] + 1});
            }
        }
        return OptionalInt.empty();
    }

    Set<Long> mutualConnections(Member a, Member b) {
        Set<Long> mutual = new HashSet<>(a.connections());
        mutual.retainAll(b.connections());
        return mutual;
    }

    /** "People you may know": 2nd-degree members ranked by mutual connections, then name. */
    List<Member> peopleYouMayKnow(Member me, int limit) {
        Map<Long, Integer> mutualCount = new HashMap<>();
        for (long friend : me.connections()) {
            for (long candidate : lookup.apply(friend).connections()) {
                if (candidate != me.id() && !me.connections().contains(candidate)) mutualCount.merge(candidate, 1, Integer::sum);
            }
        }
        List<Member> result = new ArrayList<>();
        mutualCount.keySet().stream().map(lookup::apply)
                .filter(m -> !pendingBetween(me, m))
                .sorted(Comparator.comparing((Member m) -> -mutualCount.get(m.id())).thenComparing(Member::name))
                .limit(limit).forEach(result::add);
        return result;
    }
}
