package lld.ch18.facebook;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;
import java.util.stream.Stream;

// ------------------------------------------------------------------ friends

enum RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELED }

/** Records BOTH ends; an invitation that only knows the invitee cannot be accepted. */
final class FriendRequest {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final Member from;
    private final Member to;
    private RequestStatus status = RequestStatus.PENDING;

    FriendRequest(Member from, Member to) { this.from = from; this.to = to; }
    long id() { return id; }
    Member from() { return from; }
    Member to() { return to; }
    synchronized RequestStatus status() { return status; }
    synchronized void moveTo(RequestStatus next) {
        if (status != RequestStatus.PENDING) throw new IllegalStateException("request is already " + status);
        status = next;
    }
}

/** Owns the friend graph. Accepting changes two members, so it lives here, not on either member. */
final class FriendService {
    private final Map<Long, Set<Long>> friends = new ConcurrentHashMap<>();
    private final Map<Long, FriendRequest> requests = new ConcurrentHashMap<>();
    private final NotificationService notifications;

    FriendService(NotificationService notifications) { this.notifications = notifications; }

    synchronized FriendRequest sendRequest(Member from, Member to) {
        if (from.id() == to.id()) throw new IllegalArgumentException("cannot befriend yourself");
        if (areFriends(from, to)) throw new IllegalStateException(from + " and " + to + " are already friends");
        if (pendingBetween(from.id(), to.id())) throw new IllegalStateException("a request between " + from + " and " + to + " is pending");
        FriendRequest r = new FriendRequest(from, to);
        requests.put(r.id(), r);
        notifications.send(to, NotificationType.FRIEND_REQUEST, from + " sent you a friend request");
        return r;
    }

    synchronized void accept(FriendRequest r, Member by) {
        if (by.id() != r.to().id()) throw new IllegalStateException("only " + r.to() + " can accept");
        r.moveTo(RequestStatus.ACCEPTED);
        friends.computeIfAbsent(r.from().id(), k -> ConcurrentHashMap.newKeySet()).add(r.to().id());
        friends.computeIfAbsent(r.to().id(), k -> ConcurrentHashMap.newKeySet()).add(r.from().id());
    }

    synchronized void reject(FriendRequest r, Member by) {
        if (by.id() != r.to().id()) throw new IllegalStateException("only " + r.to() + " can reject");
        r.moveTo(RequestStatus.REJECTED);
    }

    synchronized void unfriend(Member a, Member b) {
        friends.getOrDefault(a.id(), Set.of()).remove(b.id());
        friends.getOrDefault(b.id(), Set.of()).remove(a.id());
    }

    boolean areFriends(Member a, Member b) { return friendsOf(a.id()).contains(b.id()); }
    Set<Long> friendsOf(long memberId) { return Set.copyOf(friends.getOrDefault(memberId, Set.of())); }

    /** Looks BOTH ways: a request from a to b or from b to a. */
    boolean pendingBetween(long a, long b) {
        return requests.values().stream().anyMatch(r -> r.status() == RequestStatus.PENDING
                && ((r.from().id() == a && r.to().id() == b) || (r.from().id() == b && r.to().id() == a)));
    }
}

// ------------------------------------------------------------------ privacy

/**
 * The ONE place that decides "can this viewer see this post?". The post knows
 * its privacy list; only the friend graph knows friendship; this asks both.
 * Feed, search and profile pages all call it, so the rule is written once.
 */
final class VisibilityService {
    private final FriendService friends;
    VisibilityService(FriendService friends) { this.friends = friends; }

    boolean canSee(Member viewer, Post post) {
        Member author = post.author();
        if (author.status() != AccountStatus.ACTIVE) return false;
        if (viewer.id() == author.id()) return true;
        if (post.group().isPresent()) {
            Group g = post.group().get();
            return g.isPublic() || g.isMember(viewer.id());
        }
        return switch (post.audience()) {
            case Audience.Public p -> true;
            case Audience.Friends f -> friends.areFriends(viewer, author);
            case Audience.Custom c -> friends.areFriends(viewer, author) && post.isAllowedByPrivacyList(viewer.id());
            case Audience.OnlyMe o -> false;
        };
    }
}

// ------------------------------------------------------------------ notifications

enum NotificationType { FRIEND_REQUEST, NEW_MESSAGE, NEW_COMMENT, PAGE_UPDATE }

record Notification(NotificationType type, String text, long at) {}

/** Strategy for delivery. A new channel is a new class, not a new if-branch. */
interface NotificationChannel { void deliver(Member to, Notification n); }

final class PushChannel implements NotificationChannel {
    @Override public void deliver(Member to, Notification n) {
        to.notify(n);
        System.out.println("    [push -> " + to + "] " + n.text());
    }
}

final class EmailChannel implements NotificationChannel {
    @Override public void deliver(Member to, Notification n) {
        System.out.println("    [email -> " + to.email() + "] " + n.text());
    }
}

final class NotificationService {
    private final Map<NotificationType, List<NotificationChannel>> routes;
    NotificationService(Map<NotificationType, List<NotificationChannel>> routes) { this.routes = Map.copyOf(routes); }
    void send(Member to, NotificationType type, String text) {
        Notification n = new Notification(type, text, Tick.next());
        routes.getOrDefault(type, List.of()).forEach(c -> c.deliver(to, n));
    }
}

// ------------------------------------------------------------------ search

/** Every key maps to a set of matches (never a single object), and post search respects privacy. */
final class SearchIndex {
    private final Map<String, Set<Member>> members = new ConcurrentHashMap<>();
    private final Map<String, Set<Group>> groups = new ConcurrentHashMap<>();
    private final Map<String, Set<Page>> pages = new ConcurrentHashMap<>();
    private final Map<String, Set<Post>> posts = new ConcurrentHashMap<>();
    private final VisibilityService visibility;

    SearchIndex(VisibilityService visibility) { this.visibility = visibility; }

    void add(Member m) { index(members, m.name(), m); }
    void add(Group g) { index(groups, g.name(), g); }
    void add(Page p) { index(pages, p.name(), p); }
    void add(Post p) { index(posts, p.text(), p); }

    List<Member> searchMembers(String q) { return find(members, q).sorted(Comparator.comparing(Member::name)).toList(); }
    List<Group> searchGroups(String q) { return find(groups, q).toList(); }
    List<Page> searchPages(String q) { return find(pages, q).toList(); }

    /** Takes the viewer: privacy does not stop being true inside a search. */
    List<Post> searchPosts(String word, Member viewer) {
        return find(posts, word).filter(p -> visibility.canSee(viewer, p))
                .sorted(Comparator.comparingLong(Post::createdAt).reversed()).toList();
    }

    private static <T> void index(Map<String, Set<T>> idx, String text, T item) {
        for (String w : words(text)) idx.computeIfAbsent(w, k -> ConcurrentHashMap.newKeySet()).add(item);
    }

    private static <T> Stream<T> find(Map<String, Set<T>> idx, String q) {
        List<String> ws = words(q);
        if (ws.isEmpty()) return Stream.empty();
        return idx.getOrDefault(ws.get(0), Set.of()).stream()
                .filter(item -> ws.stream().allMatch(w -> idx.getOrDefault(w, Set.of()).contains(item)));
    }

    private static List<String> words(String s) {
        return Arrays.stream(s.toLowerCase(Locale.ROOT).split("\\W+")).filter(w -> !w.isEmpty()).toList();
    }
}

// ------------------------------------------------------------------ news feed

/** Strategy for ranking the news feed. */
interface FeedRanker { Comparator<Post> order(Member viewer); }

final class RecentFirst implements FeedRanker {
    @Override public Comparator<Post> order(Member viewer) { return Comparator.comparingLong(Post::createdAt).reversed(); }
}

/** Engagement x affinity / age, a toy version of the idea behind ranked feeds. */
final class EngagementRanker implements FeedRanker {
    private final FriendService friends;
    EngagementRanker(FriendService friends) { this.friends = friends; }

    @Override public Comparator<Post> order(Member viewer) {
        return Comparator.comparingDouble((Post p) -> -score(viewer, p)).thenComparing(Comparator.comparingLong(Post::createdAt).reversed());
    }

    private double score(Member viewer, Post p) {
        double engagement = 1 + p.likeCount() + 2.0 * p.comments().size();
        double affinity = friends.areFriends(viewer, p.author()) ? 2.0 : 1.0;
        return engagement * affinity / (1 + (Tick.now() - p.createdAt()) / 10.0);
    }
}

/**
 * Pull model: gather candidates (friends, followed members, followed pages,
 * joined groups), drop what the viewer may not see, rank, cut to size.
 */
final class NewsFeedService {
    private final FriendService friends;
    private final VisibilityService visibility;
    private final LongFunction<Member> members;
    private final LongFunction<Page> pages;
    private final LongFunction<Group> groups;

    NewsFeedService(FriendService f, VisibilityService v, LongFunction<Member> m, LongFunction<Page> p, LongFunction<Group> g) {
        friends = f; visibility = v; members = m; pages = p; groups = g;
    }

    List<Post> feed(Member viewer, FeedRanker ranker, int limit) {
        Set<Long> people = new HashSet<>(friends.friendsOf(viewer.id()));
        people.addAll(viewer.followingMembers());
        Stream<Post> fromPeople = people.stream().map(members::apply).flatMap(m -> m.posts().stream());
        Stream<Post> fromPages = viewer.followingPages().stream().map(pages::apply).flatMap(pg -> pg.posts().stream());
        Stream<Post> fromGroups = viewer.groups().stream().map(groups::apply).flatMap(g -> g.posts().stream());
        return Stream.of(fromPeople, fromPages, fromGroups).flatMap(s -> s)
                .distinct()
                .filter(p -> p.author().id() != viewer.id())
                .filter(p -> visibility.canSee(viewer, p))
                .sorted(ranker.order(viewer))
                .limit(limit)
                .toList();
    }
}

// ------------------------------------------------------------------ suggestions (extended requirement)

record Suggestion(Member member, int mutualFriends) {
    @Override public String toString() { return member.name() + "(" + mutualFriends + " mutual)"; }
}

/** Friends of friends, scored by mutual friends; never yourself, a friend, or anyone with a pending request. */
final class SuggestionService {
    private final FriendService friends;
    private final LongFunction<Member> members;

    SuggestionService(FriendService friends, LongFunction<Member> members) { this.friends = friends; this.members = members; }

    List<Suggestion> suggest(Member me, int limit) {
        Set<Long> mine = friends.friendsOf(me.id());
        Map<Long, Integer> mutual = new HashMap<>();
        for (long f : mine) {
            for (long candidate : friends.friendsOf(f)) {
                if (candidate == me.id() || mine.contains(candidate) || friends.pendingBetween(me.id(), candidate)) continue;
                mutual.merge(candidate, 1, Integer::sum);
            }
        }
        return mutual.entrySet().stream()
                .map(e -> new Suggestion(members.apply(e.getKey()), e.getValue()))
                .sorted(Comparator.comparingInt(Suggestion::mutualFriends).reversed()
                        .thenComparingLong(s -> s.member().id()))            // stable tie-break
                .limit(limit)
                .toList();
    }
}
