package lld.ch18.linkedin;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Facade that wires the services and checks account status before every action. */
final class LinkedIn {
    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final AtomicLong messageIds = new AtomicLong();
    final NotificationService notifications;
    final ConnectionService connections;
    final SearchIndex search = new SearchIndex();
    final FeedService feeds;

    LinkedIn(List<NotificationChannel> channels) {
        notifications = new NotificationService(channels);
        connections = new ConnectionService(notifications, this::member);
        feeds = new FeedService(this::member);
    }

    Member member(long id) {
        Member m = members.get(id);
        if (m == null) throw new NoSuchElementException("no member " + id);
        return m;
    }

    <M extends Member> M join(M m) {
        members.put(m.id(), m);
        search.addMember(m);
        return m;
    }

    /** The code that changes a name must tell the index, or search goes stale. */
    void rename(Member m, String newName) {
        String old = m.name();
        search.removeMember(m, old);
        m.rename(newName);
        search.addMember(m);
    }

    List<Member> searchMembers(Member viewer, String query) {
        List<Member> found = search.searchMembers(query);
        found.stream().filter(m -> m != viewer).forEach(m -> m.profile().stats().recordSearchAppearance());
        return found;
    }

    void viewProfile(Member viewer, Member owner) {
        if (viewer != owner) owner.profile().stats().recordView();
    }

    void endorse(Member by, Member owner, String skillName) {
        active(by);
        if (!by.isConnectedTo(owner)) throw new IllegalStateException("only 1st-degree connections can endorse " + owner);
        Skill s = owner.profile().skill(skillName).orElseThrow(() -> new NoSuchElementException(owner + " has no skill " + skillName));
        if (!s.endorse(by.id())) throw new IllegalStateException(by + " already endorsed " + skillName);
    }

    Recommendation requestRecommendation(Member requester, Member author) {
        active(requester);
        if (!requester.isConnectedTo(author)) throw new IllegalStateException("you can only ask your connections");
        Recommendation r = new Recommendation(author.id(), author.name());
        requester.profile().addRecommendation(r);
        notifications.send(author, NotificationType.RECOMMENDATION_REQUEST, requester + " asked you for a recommendation");
        return r;
    }

    Post post(Member author, String text) {
        active(author);
        Post p = new Post(author, text, null);
        author.addPost(p);
        return p;
    }

    Post share(Member by, Post original, String text) {
        active(by);
        Post p = new Post(by, text, original);
        original.recordShare();
        by.addPost(p);
        return p;
    }

    Comment comment(Member by, Post post, String text) {
        active(by);
        Comment c = post.addComment(by, text);
        if (post.author() != by) notifications.send(post.author(), NotificationType.NEW_COMMENT, by + " commented: \"" + text + "\"");
        return c;
    }

    Message message(Member from, List<Member> to, String text) {
        active(from);
        for (Member r : to) {
            if (!from.isConnectedTo(r)) throw new IllegalStateException(r + " is not a connection (that would need InMail)");
        }
        Message m = new Message(messageIds.incrementAndGet(), from, List.copyOf(to), text, Tick.next());
        for (Member r : to) {
            r.receive(m);
            notifications.send(r, NotificationType.NEW_MESSAGE, "from " + from + ": " + text);
        }
        return m;
    }

    Company createCompany(Member by, String name) {
        active(by);
        Company c = new Company(name, by);
        search.addCompany(c);
        return c;
    }

    JobPosting postJob(Member by, Company c, String title, String location, EmploymentType type) {
        JobPosting j = c.postJob(by, title, location, type);
        search.addJob(j);
        return j;
    }

    void followCompany(Member m, Company c) {
        if (m.followCompany(c)) c.addFollower(m.id());
    }

    void block(Admin admin, Member target) { target.setStatus(AccountStatus.BLOCKED); }

    private static void active(Member m) {
        if (m.status() != AccountStatus.ACTIVE) throw new IllegalStateException(m + " is " + m.status());
    }
}
