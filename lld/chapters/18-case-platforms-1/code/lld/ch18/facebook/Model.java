package lld.ch18.facebook;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Deterministic logical clock for repeatable demo output. */
final class Tick {
    private static final AtomicLong NOW = new AtomicLong();
    static long next() { return NOW.incrementAndGet(); }
    static long now() { return NOW.get(); }
    private Tick() {}
}

record Work(String title, String company, String location, int fromYear, Integer toYear) {
    boolean isCurrent() { return toYear == null; }
}
record Education(String school, String degree, int fromYear, int toYear) {}
record Place(String city, String country, int fromYear) {}

/** Every field has a default, so a profile is usable the moment the account exists. */
final class Profile {
    private volatile String picture = "default-avatar.png";
    private volatile String bio = "";
    private final List<Work> work = new CopyOnWriteArrayList<>();
    private final List<Education> education = new CopyOnWriteArrayList<>();
    private final List<Place> places = new CopyOnWriteArrayList<>();

    String picture() { return picture; }
    void setPicture(String p) { picture = p; }
    String bio() { return bio; }
    void setBio(String b) { bio = b; }
    void addWork(Work w) { work.add(w); }
    void addEducation(Education e) { education.add(e); }
    void addPlace(Place p) { places.add(p); }
    List<Work> work() { return List.copyOf(work); }
    List<Place> places() { return List.copyOf(places); }
}

enum AccountStatus { ACTIVE, BLOCKED, BANNED, COMPROMISED, ARCHIVED, UNKNOWN }

class Member {
    private static final AtomicLong IDS = new AtomicLong();
    private final long id = IDS.incrementAndGet();
    private final String name;
    private final String email;
    private final Profile profile = new Profile();
    private volatile AccountStatus status = AccountStatus.ACTIVE;
    private final Set<Long> followingMembers = ConcurrentHashMap.newKeySet();   // one-sided
    private final Set<Long> followingPages = ConcurrentHashMap.newKeySet();
    private final Set<Long> groups = ConcurrentHashMap.newKeySet();
    private final List<Post> posts = new CopyOnWriteArrayList<>();
    private final List<PrivacyList> privacyLists = new CopyOnWriteArrayList<>();
    private final List<Message> inbox = new CopyOnWriteArrayList<>();
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    Member(String name, String email) { this.name = name; this.email = email; }

    long id() { return id; }
    String name() { return name; }
    String email() { return email; }
    Profile profile() { return profile; }
    AccountStatus status() { return status; }
    void setStatus(AccountStatus s) { status = s; }

    boolean follow(Member m) { return m.id != id && followingMembers.add(m.id); }
    boolean unfollow(Member m) { return followingMembers.remove(m.id); }
    Set<Long> followingMembers() { return Set.copyOf(followingMembers); }
    boolean followPage(Page p) { return followingPages.add(p.id()); }
    Set<Long> followingPages() { return Set.copyOf(followingPages); }
    void joinedGroup(Group g) { groups.add(g.id()); }
    Set<Long> groups() { return Set.copyOf(groups); }

    void addPost(Post p) { posts.add(p); }
    List<Post> posts() { return List.copyOf(posts); }
    void addPrivacyList(PrivacyList l) { privacyLists.add(l); }
    List<PrivacyList> privacyLists() { return List.copyOf(privacyLists); }
    void receive(Message m) { inbox.add(m); }
    List<Message> inbox() { return List.copyOf(inbox); }
    void notify(Notification n) { notifications.add(n); }
    List<Notification> notifications() { return List.copyOf(notifications); }

    @Override public String toString() { return name; }
}

final class Admin extends Member {
    Admin(String name, String email) { super(name, email); }
}

/** A named subset of the owner's friends, e.g. "Close friends". Outlives any single post. */
final class PrivacyList {
    private final Member owner;
    private final String name;
    private final Set<Long> members = ConcurrentHashMap.newKeySet();

    PrivacyList(Member owner, String name) { this.owner = owner; this.name = name; }
    Member owner() { return owner; }
    String name() { return name; }
    void add(long memberId) { members.add(memberId); }
    boolean contains(long memberId) { return members.contains(memberId); }
}

/** Who may see a post. A sealed type makes the visibility switch exhaustive. */
sealed interface Audience {
    record Public() implements Audience {}
    record Friends() implements Audience {}
    record Custom(PrivacyList list) implements Audience {}
    record OnlyMe() implements Audience {}
}

abstract class SocialContent {
    private static final AtomicLong IDS = new AtomicLong(1000);
    private final long id = IDS.incrementAndGet();
    private final Member author;
    private final String text;
    private final long createdAt = Tick.next();
    private final Set<Long> likes = new LinkedHashSet<>();      // a set: no double likes, can be undone

    SocialContent(Member author, String text) { this.author = author; this.text = text; }

    long id() { return id; }
    Member author() { return author; }
    String text() { return text; }
    long createdAt() { return createdAt; }
    synchronized boolean like(long memberId) { return likes.add(memberId); }
    synchronized boolean unlike(long memberId) { return likes.remove(memberId); }
    synchronized int likeCount() { return likes.size(); }
}

final class Post extends SocialContent {
    private final Audience audience;
    private final Group group;          // null unless posted in a group
    private final Page page;            // null unless published by a page
    private final Post sharedFrom;
    private final List<Comment> comments = new ArrayList<>();

    Post(Member author, String text, Audience audience, Group group, Page page, Post sharedFrom) {
        super(author, text);
        this.audience = audience;
        this.group = group;
        this.page = page;
        this.sharedFrom = sharedFrom;
    }

    Audience audience() { return audience; }
    Optional<Group> group() { return Optional.ofNullable(group); }
    Optional<Page> page() { return Optional.ofNullable(page); }
    Optional<Post> sharedFrom() { return Optional.ofNullable(sharedFrom); }

    /** The half of the privacy rule the post CAN answer on its own. */
    boolean isAllowedByPrivacyList(long viewerId) {
        return !(audience instanceof Audience.Custom c) || c.list().contains(viewerId);
    }

    synchronized Comment addComment(Member by, String text) {
        Comment c = new Comment(by, text, this);
        comments.add(c);
        return c;
    }
    synchronized List<Comment> comments() { return List.copyOf(comments); }

    @Override public String toString() {
        String where = page != null ? " [page " + page.name() + "]" : group != null ? " [group " + group.name() + "]" : "";
        return "\"" + text() + "\" by " + author().name() + where;
    }
}

final class Comment extends SocialContent {
    private final Post post;
    Comment(Member author, String text, Post post) { super(author, text); this.post = post; }
    Post post() { return post; }
}

record Message(Member sender, List<Member> recipients, String text, long sentAt) {}
