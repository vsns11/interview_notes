package lld.ch18.linkedin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Posts AND comments can be liked, so likes live in a shared base class, as a set of member ids. */
abstract class SocialContent {
    private static final AtomicLong IDS = new AtomicLong(1000);
    private final long id = IDS.incrementAndGet();
    private final Member author;
    private final String text;
    private final long createdAt = Tick.next();
    private final Set<Long> likes = new LinkedHashSet<>();

    SocialContent(Member author, String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text cannot be empty");
        this.author = author;
        this.text = text;
    }

    long id() { return id; }
    Member author() { return author; }
    String text() { return text; }
    long createdAt() { return createdAt; }
    synchronized boolean like(long memberId) { return likes.add(memberId); }
    synchronized boolean unlike(long memberId) { return likes.remove(memberId); }
    synchronized int likeCount() { return likes.size(); }
}

final class Post extends SocialContent {
    private final List<Comment> comments = new ArrayList<>();
    private final Post sharedFrom;                  // null for an original post
    private final AtomicInteger shares = new AtomicInteger();

    Post(Member author, String text, Post sharedFrom) {
        super(author, text);
        this.sharedFrom = sharedFrom;
    }

    synchronized Comment addComment(Member by, String text) {
        Comment c = new Comment(by, text, this);
        comments.add(c);
        return c;
    }
    synchronized List<Comment> comments() { return List.copyOf(comments); }
    Optional<Post> sharedFrom() { return Optional.ofNullable(sharedFrom); }
    void recordShare() { shares.incrementAndGet(); }
    int shares() { return shares.get(); }

    @Override public String toString() {
        return "\"" + text() + "\" by " + author().name()
                + sharedFrom().map(p -> " (shared from " + p.author().name() + ")").orElse("");
    }
}

final class Comment extends SocialContent {
    private final Post post;
    Comment(Member author, String text, Post post) { super(author, text); this.post = post; }
    Post post() { return post; }
}

/** Sent to people, never posted; nobody likes a message, so it is not SocialContent. */
record Message(long id, Member sender, List<Member> recipients, String text, long sentAt) {}
