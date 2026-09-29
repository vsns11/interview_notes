package lld.ch18.facebook;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Facade: wires the services and checks account status on every write. */
final class Facebook {
    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final Map<Long, Page> pages = new ConcurrentHashMap<>();
    private final Map<Long, Group> groups = new ConcurrentHashMap<>();
    final NotificationService notifications;
    final FriendService friends;
    final VisibilityService visibility;
    final SearchIndex search;
    final NewsFeedService feed;
    final SuggestionService suggestions;

    Facebook(Map<NotificationType, List<NotificationChannel>> routes) {
        notifications = new NotificationService(routes);
        friends = new FriendService(notifications);
        visibility = new VisibilityService(friends);
        search = new SearchIndex(visibility);
        feed = new NewsFeedService(friends, visibility, members::get, pages::get, groups::get);
        suggestions = new SuggestionService(friends, members::get);
    }

    <M extends Member> M register(M m) {
        members.put(m.id(), m);
        search.add(m);
        return m;
    }

    PrivacyList createPrivacyList(Member owner, String name, List<Member> people) {
        PrivacyList list = new PrivacyList(owner, name);
        for (Member p : people) {
            if (!friends.areFriends(owner, p)) throw new IllegalStateException(p + " is not a friend of " + owner);
            list.add(p.id());
        }
        owner.addPrivacyList(list);
        return list;
    }

    Post post(Member author, String text, Audience audience) {
        active(author);
        if (audience instanceof Audience.Custom c && c.list().owner() != author)
            throw new IllegalArgumentException("you can only use your own privacy lists");
        Post p = new Post(author, text, audience, null, null, null);
        author.addPost(p);
        search.add(p);
        return p;
    }

    Post share(Member by, Post original, Audience audience) {
        active(by);
        if (!visibility.canSee(by, original)) throw new IllegalStateException(by + " cannot see that post");
        Post p = new Post(by, "shared: " + original.text(), audience, null, null, original);
        by.addPost(p);
        return p;
    }

    Comment comment(Member by, Post post, String text) {
        active(by);
        if (!visibility.canSee(by, post)) throw new IllegalStateException(by + " cannot see that post");
        Comment c = post.addComment(by, text);
        if (post.author() != by) notifications.send(post.author(), NotificationType.NEW_COMMENT, by + " commented on your post");
        return c;
    }

    boolean like(Member by, SocialContent content, Post containingPost) {
        active(by);
        if (!visibility.canSee(by, containingPost)) throw new IllegalStateException(by + " cannot see that post");
        return content.like(by.id());
    }

    void message(Member from, List<Member> to, String text) {
        active(from);
        Message m = new Message(from, List.copyOf(to), text, Tick.next());
        for (Member r : to) {
            r.receive(m);
            notifications.send(r, NotificationType.NEW_MESSAGE, "new message from " + from);
        }
    }

    Page createPage(Member owner, String name, PageType type) {
        active(owner);
        Page p = new Page(name, type, owner);
        pages.put(p.id(), p);
        search.add(p);
        return p;
    }

    void followPage(Member m, Page p) {
        if (m.followPage(p)) p.addFollower(m.id());
    }

    Post publishOnPage(Member by, Page page, String text) {
        Post p = page.publish(by, text);
        search.add(p);
        return p;
    }

    Group createGroup(Member owner, String name, GroupPrivacy privacy) {
        active(owner);
        Group g = new Group(name, privacy, owner);
        groups.put(g.id(), g);
        search.add(g);
        return g;
    }

    Post postInGroup(Member by, Group g, String text) {
        active(by);
        Post p = g.post(by, text);
        search.add(p);
        return p;
    }

    void block(Admin admin, Member target) { target.setStatus(AccountStatus.BLOCKED); }

    private static void active(Member m) {
        if (m.status() != AccountStatus.ACTIVE) throw new IllegalStateException(m + " is " + m.status());
    }
}
