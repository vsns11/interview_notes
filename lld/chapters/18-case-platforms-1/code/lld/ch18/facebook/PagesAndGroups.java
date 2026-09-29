package lld.ch18.facebook;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

enum PageType { BUSINESS, BRAND, PUBLIC_FIGURE, COMMUNITY }

/** One recommendation per member per page, with a 1-5 rating. */
record PageRecommendation(long memberId, int rating, String text) {
    PageRecommendation {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1..5");
    }
}

final class Page {
    private static final AtomicLong IDS = new AtomicLong(500);
    private final long id = IDS.incrementAndGet();
    private final String name;
    private final PageType type;
    private final Member owner;
    private final Set<Long> followers = ConcurrentHashMap.newKeySet();
    private final Map<Long, PageRecommendation> recommendations = new ConcurrentHashMap<>();   // owned
    private final List<Post> posts = new CopyOnWriteArrayList<>();

    Page(String name, PageType type, Member owner) { this.name = name; this.type = type; this.owner = owner; }

    long id() { return id; }
    String name() { return name; }
    PageType type() { return type; }
    Member owner() { return owner; }
    void addFollower(long memberId) { followers.add(memberId); }
    int followerCount() { return followers.size(); }            // never store what you can count

    void recommend(Member by, int rating, String text) {
        if (recommendations.putIfAbsent(by.id(), new PageRecommendation(by.id(), rating, text)) != null)
            throw new IllegalStateException(by + " already recommended " + name);
    }

    OptionalDouble averageRating() {
        return recommendations.values().stream().mapToInt(PageRecommendation::rating).average();
    }
    int recommendationCount() { return recommendations.size(); }

    Post publish(Member by, String text) {
        if (by.id() != owner.id()) throw new IllegalStateException(by + " does not manage page " + name);
        Post p = new Post(by, text, new Audience.Public(), null, this, null);
        posts.add(p);
        return p;
    }
    List<Post> posts() { return List.copyOf(posts); }
}

enum GroupPrivacy { PUBLIC, PRIVATE }

final class Group {
    private static final AtomicLong IDS = new AtomicLong(900);
    private final long id = IDS.incrementAndGet();
    private final String name;
    private final GroupPrivacy privacy;
    private final Member owner;
    private final Set<Long> members = ConcurrentHashMap.newKeySet();
    private final Set<Long> joinRequests = ConcurrentHashMap.newKeySet();
    private final List<Post> posts = new CopyOnWriteArrayList<>();

    Group(String name, GroupPrivacy privacy, Member owner) {
        this.name = name;
        this.privacy = privacy;
        this.owner = owner;
        members.add(owner.id());
        owner.joinedGroup(this);
    }

    long id() { return id; }
    String name() { return name; }
    boolean isPublic() { return privacy == GroupPrivacy.PUBLIC; }
    boolean isMember(long memberId) { return members.contains(memberId); }
    int memberCount() { return members.size(); }

    /** Public groups: join at once. Private groups: the request waits for the owner. Returns true if joined now. */
    boolean requestToJoin(Member m) {
        if (isPublic()) { members.add(m.id()); m.joinedGroup(this); return true; }
        joinRequests.add(m.id());
        return false;
    }

    void approve(Member by, Member requester) {
        if (by.id() != owner.id()) throw new IllegalStateException("only the owner approves members");
        if (!joinRequests.remove(requester.id())) throw new IllegalStateException(requester + " did not ask to join");
        members.add(requester.id());
        requester.joinedGroup(this);
    }

    Post post(Member by, String text) {
        if (!isMember(by.id())) throw new IllegalStateException(by + " is not a member of " + name);
        Post p = new Post(by, text, new Audience.Public(), this, null, null);
        posts.add(p);
        return p;
    }
    List<Post> posts() { return List.copyOf(posts); }
}
