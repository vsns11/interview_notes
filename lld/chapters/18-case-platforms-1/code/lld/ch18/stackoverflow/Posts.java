package lld.ch18.stackoverflow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Behaviour shared by questions, answers and comments: votes, flags and comments.
 * Votes are stored per voter (not as a bare counter) so one member cannot vote twice
 * and a vote can be changed or reversed.
 */
abstract sealed class Post permits Question, Answer, Comment {
    private static final AtomicLong IDS = new AtomicLong(100);

    private final long id = IDS.incrementAndGet();
    private final Member author;
    private volatile String body;
    private final Map<Long, VoteType> votes = new HashMap<>();
    private final Set<Long> flaggers = new HashSet<>();
    private final List<Comment> comments = new ArrayList<>();

    Post(Member author, String body) {
        this.author = author;
        this.body = body;
    }

    long id() { return id; }
    Member author() { return author; }
    String body() { return body; }
    void edit(String newBody) { this.body = newBody; }

    /** Records or changes a vote and returns the voter's previous vote, if any. */
    synchronized Optional<VoteType> castVote(Member voter, VoteType type) {
        if (voter.id() == author.id()) throw new IllegalStateException("you cannot vote on your own post");
        VoteType previous = votes.put(voter.id(), type);
        if (previous == type) throw new IllegalStateException(voter.name() + " already voted " + type + " on this post");
        return Optional.ofNullable(previous);
    }

    synchronized int score() {
        return votes.values().stream().mapToInt(v -> v == VoteType.UP ? 1 : -1).sum();
    }

    /** Returns the number of distinct flaggers after this flag. */
    synchronized int flag(Member by) {
        if (!flaggers.add(by.id())) throw new IllegalStateException(by.name() + " already flagged this post");
        return flaggers.size();
    }

    synchronized void addComment(Comment c) { comments.add(c); }
    synchronized List<Comment> comments() { return List.copyOf(comments); }

    abstract String label();
}

final class Question extends Post {
    private final String title;
    private final Set<Tag> tags;
    private final List<Answer> answers = new ArrayList<>();
    private final Set<Long> closeVoters = new HashSet<>();
    private final Set<Long> deleteVoters = new HashSet<>();
    private final AtomicInteger views = new AtomicInteger();
    private QuestionStatus status = QuestionStatus.OPEN;
    private ClosingRemark closingRemark;
    private Bounty bounty;

    Question(Member author, String title, String body, Set<Tag> tags) {
        super(author, body);
        this.title = title;
        this.tags = new LinkedHashSet<>(tags);
        tags.forEach(Tag::recordUse);            // a tag counts its own uses: "top tags" becomes a read
    }

    String title() { return title; }
    Set<Tag> tags() { return Set.copyOf(tags); }
    int view() { return views.incrementAndGet(); }
    int views() { return views.get(); }

    synchronized QuestionStatus status() { return status; }
    synchronized Optional<ClosingRemark> closingRemark() { return Optional.ofNullable(closingRemark); }
    synchronized List<Answer> answers() { return List.copyOf(answers); }
    synchronized Optional<Bounty> bounty() { return Optional.ofNullable(bounty); }

    synchronized void addAnswer(Answer a) {
        if (status != QuestionStatus.OPEN) throw new IllegalStateException("question is " + status + ", no new answers");
        answers.add(a);
    }

    /** Marks one answer accepted and returns the previously accepted one (only one answer can win). */
    synchronized Optional<Answer> accept(Answer chosen) {
        if (!answers.contains(chosen)) throw new IllegalArgumentException("answer does not belong to this question");
        Optional<Answer> previous = answers.stream().filter(Answer::isAccepted).findFirst();
        previous.ifPresent(p -> p.setAccepted(false));
        chosen.setAccepted(true);
        return previous;
    }

    synchronized void attachBounty(Bounty b) {
        if (status != QuestionStatus.OPEN) throw new IllegalStateException("bounties need an open question");
        if (bounty != null && !bounty.isSettled()) throw new IllegalStateException("question already has an active bounty");
        bounty = b;
    }

    /** One close vote per member; returns true when this vote closed the question. */
    synchronized boolean voteToClose(Member voter, ClosingRemark remark, int threshold) {
        if (status != QuestionStatus.OPEN) throw new IllegalStateException("question is already " + status);
        if (!closeVoters.add(voter.id())) throw new IllegalStateException(voter.name() + " already voted to close");
        if (closeVoters.size() >= threshold) { close(remark); return true; }
        return false;
    }

    synchronized boolean voteToDelete(Member voter, int threshold) {
        if (status != QuestionStatus.CLOSED) throw new IllegalStateException("only closed questions can be deleted by vote");
        if (!deleteVoters.add(voter.id())) throw new IllegalStateException(voter.name() + " already voted to delete");
        if (deleteVoters.size() >= threshold) { status = QuestionStatus.DELETED; return true; }
        return false;
    }

    synchronized void close(ClosingRemark remark) { status = QuestionStatus.CLOSED; closingRemark = remark; }
    synchronized void reopen() { status = QuestionStatus.OPEN; closingRemark = null; closeVoters.clear(); }
    synchronized void delete() { status = QuestionStatus.DELETED; }
    synchronized void undelete() { status = QuestionStatus.CLOSED; deleteVoters.clear(); }

    @Override String label() { return "question #" + id() + " \"" + title + "\""; }
}

final class Answer extends Post {
    private final Question question;
    private volatile boolean accepted;

    Answer(Member author, Question question, String body) {
        super(author, body);
        this.question = question;
    }

    Question question() { return question; }
    boolean isAccepted() { return accepted; }
    void setAccepted(boolean accepted) { this.accepted = accepted; }

    @Override String label() { return "answer #" + id() + " by " + author().name(); }
}

final class Comment extends Post {
    private final Post parent;

    Comment(Member author, Post parent, String body) {
        super(author, body);
        this.parent = parent;
    }

    Post parent() { return parent; }
    @Override String label() { return "comment #" + id(); }
}

/** A tag is shared by many questions and outlives each of them (association, not composition). */
final class Tag {
    private final String name;
    private final String description;
    private final AtomicInteger uses = new AtomicInteger();

    Tag(String name, String description) { this.name = name; this.description = description; }

    String name() { return name; }
    String description() { return description; }
    void recordUse() { uses.incrementAndGet(); }
    int uses() { return uses.get(); }
    @Override public String toString() { return "[" + name + "]"; }
}

/** Reputation the asker has staked; it leaves the asker's account when the bounty starts. */
final class Bounty {
    private final Member sponsor;
    private final int reputation;
    private final LocalDate expiresOn;
    private Member awardedTo;
    private boolean expired;

    Bounty(Member sponsor, int reputation, LocalDate expiresOn) {
        this.sponsor = sponsor;
        this.reputation = reputation;
        this.expiresOn = expiresOn;
    }

    Member sponsor() { return sponsor; }
    int reputation() { return reputation; }
    LocalDate expiresOn() { return expiresOn; }
    synchronized boolean isSettled() { return awardedTo != null || expired; }

    synchronized void awardTo(Member winner, LocalDate today) {
        if (isSettled()) throw new IllegalStateException("bounty already settled");
        if (today.isAfter(expiresOn)) throw new IllegalStateException("bounty expired on " + expiresOn);
        awardedTo = winner;
    }

    synchronized void expire() { expired = true; }
}
