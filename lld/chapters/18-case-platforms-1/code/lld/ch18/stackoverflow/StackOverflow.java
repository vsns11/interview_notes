package lld.ch18.stackoverflow;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Facade: the one entry point the UI/controllers call. It checks who may do
 * what (account status + privilege), delegates state changes to the posts,
 * applies reputation through the policy, and tells listeners.
 */
public final class StackOverflow {
    static final int VOTES_TO_CLOSE = 3;
    static final int VOTES_TO_DELETE = 3;
    static final int FLAGS_FOR_MODERATOR_REVIEW = 3;
    static final int MIN_BOUNTY = 50;
    static final int MAX_BOUNTY = 500;
    static final int BOUNTY_DAYS = 7;

    private final Map<String, Tag> tags = new ConcurrentHashMap<>();
    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final QuestionIndex index = new QuestionIndex();
    private final ReputationPolicy policy;
    private final Notifier notifier;
    private final List<ReputationListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Moderator> moderators = new CopyOnWriteArrayList<>();

    StackOverflow(ReputationPolicy policy, Notifier notifier) {
        this.policy = policy;
        this.notifier = notifier;
    }

    void addReputationListener(ReputationListener l) { listeners.add(l); }

    // ---------- accounts ----------
    <M extends Member> M register(M member) {
        members.put(member.id(), member);
        if (member instanceof Moderator mod) moderators.add(mod);
        return member;
    }

    void block(Admin admin, Member target) { target.setStatus(AccountStatus.BLOCKED); log(admin.name() + " blocked " + target.name()); }
    void unblock(Admin admin, Member target) { target.setStatus(AccountStatus.ACTIVE); log(admin.name() + " unblocked " + target.name()); }

    // ---------- guest operations (no Member needed) ----------
    List<Question> search(String query) { return index.search(query); }
    List<Question> searchByTag(String tag) { return index.searchByTag(tag); }
    int view(Question q) { return q.view(); }

    /** Requirement 13: the most used tags; each tag counted itself, so this is a sort, not a scan of all questions. */
    List<Tag> topTags(int n) {
        return tags.values().stream()
                .sorted(Comparator.comparingInt(Tag::uses).reversed().thenComparing(Tag::name))
                .limit(n).toList();
    }

    // ---------- tags ----------
    Tag createTag(Member by, String name, String description) {
        require(by, Privilege.CREATE_TAG);
        return tags.computeIfAbsent(name.toLowerCase(Locale.ROOT), k -> new Tag(k, description));
    }

    // ---------- posting ----------
    Question ask(Member asker, String title, String body, List<String> tagNames) {
        require(asker, Privilege.ASK_OR_ANSWER);
        Set<Tag> resolved = new LinkedHashSet<>();
        for (String raw : tagNames) {
            String name = raw.toLowerCase(Locale.ROOT);
            Tag tag = tags.get(name);
            if (tag == null) tag = createTag(asker, name, "");    // throws if the asker may not create tags
            resolved.add(tag);
        }
        Question q = new Question(asker, title, body, resolved);
        index.add(q);
        log(asker.name() + " asked " + q.label() + " " + resolved);
        return q;
    }

    Answer answer(Member author, Question q, String body) {
        require(author, Privilege.ASK_OR_ANSWER);
        Answer a = new Answer(author, q, body);
        q.addAnswer(a);                                          // throws unless the question is OPEN
        log(author.name() + " posted " + a.label() + " on question #" + q.id());
        notifier.send(q.author(), author.name() + " answered \"" + q.title() + "\"");
        return a;
    }

    Comment comment(Member author, Post target, String body) {
        requireActive(author);
        boolean ownThread = target.author().id() == author.id()
                || (target instanceof Answer a && a.question().author().id() == author.id());
        if (!ownThread) require(author, Privilege.COMMENT_EVERYWHERE);
        Comment c = new Comment(author, target, body);
        target.addComment(c);
        log(author.name() + " commented on " + target.label() + ": \"" + body + "\"");
        if (target.author().id() != author.id()) notifier.send(target.author(), author.name() + " commented on your " + target.label());
        return c;
    }

    // ---------- voting & reputation ----------
    void vote(Member voter, Post post, VoteType type) {
        require(voter, type == VoteType.UP ? Privilege.VOTE_UP : Privilege.VOTE_DOWN);
        if (post instanceof Comment && type == VoteType.DOWN) throw new IllegalStateException("comments can only be upvoted");
        Optional<VoteType> previous = post.castVote(voter, type);
        log(voter.name() + " voted " + type + " on " + post.label() + " -> score " + post.score());
        previous.ifPresent(p -> {                                  // undo the old vote's effect first
            adjust(post.author(), -policy.authorDeltaForVote(post, p), "vote changed on " + post.label());
            adjust(voter, -policy.voterDeltaForVote(post, p), "vote changed");
        });
        adjust(post.author(), policy.authorDeltaForVote(post, type), type + " on " + post.label());
        adjust(voter, policy.voterDeltaForVote(post, type), "downvoted an answer");
    }

    void acceptAnswer(Member asker, Answer answer) {
        requireActive(asker);
        Question q = answer.question();
        if (q.author().id() != asker.id()) throw new IllegalStateException("only the asker can accept an answer");
        Optional<Answer> previous = q.accept(answer);
        log(asker.name() + " accepted " + answer.label());
        previous.ifPresent(p -> adjust(p.author(), -policy.acceptedAnswerBonus(), "acceptance moved away"));
        adjust(answer.author(), policy.acceptedAnswerBonus(), "answer accepted");
        if (previous.isEmpty() && answer.author().id() != asker.id()) adjust(asker, policy.acceptorBonus(), "accepted an answer");
    }

    // ---------- bounties ----------
    Bounty offerBounty(Member sponsor, Question q, int amount, LocalDate today) {
        require(sponsor, Privilege.SET_BOUNTY);
        if (amount < MIN_BOUNTY || amount > MAX_BOUNTY) throw new IllegalArgumentException("bounty must be " + MIN_BOUNTY + ".." + MAX_BOUNTY);
        if (sponsor.reputation() - amount < 1) throw new IllegalStateException("not enough reputation to stake " + amount);
        Bounty b = new Bounty(sponsor, amount, today.plusDays(BOUNTY_DAYS));
        q.attachBounty(b);
        adjust(sponsor, -amount, "bounty staked");          // staked reputation leaves the sponsor now
        log(sponsor.name() + " offered a +" + amount + " bounty on question #" + q.id() + " (expires " + b.expiresOn() + ")");
        return b;
    }

    void awardBounty(Member sponsor, Answer winner, LocalDate today) {
        Bounty b = winner.question().bounty().orElseThrow(() -> new IllegalStateException("no bounty on this question"));
        if (b.sponsor().id() != sponsor.id()) throw new IllegalStateException("only the sponsor awards the bounty");
        if (winner.author().id() == sponsor.id()) throw new IllegalStateException("you cannot award your own answer");
        b.awardTo(winner.author(), today);
        adjust(winner.author(), b.reputation(), "bounty awarded");
        log(sponsor.name() + " awarded the bounty to " + winner.label());
    }

    // ---------- moderation ----------
    void flag(Member by, Post post, String reason) {
        require(by, Privilege.FLAG);
        int count = post.flag(by);
        log(by.name() + " flagged " + post.label() + " (" + reason + "), flags=" + count);
        if (count == FLAGS_FOR_MODERATOR_REVIEW) {
            moderators.forEach(m -> notifier.send(m, post.label() + " needs moderator attention"));
        }
    }

    void voteToClose(Member voter, Question q, ClosingRemark remark) {
        require(voter, Privilege.CAST_CLOSE_VOTE);
        boolean closed = q.voteToClose(voter, remark, VOTES_TO_CLOSE);
        log(voter.name() + " voted to close question #" + q.id() + " as " + remark + (closed ? " -> CLOSED" : ""));
    }

    void voteToDelete(Member voter, Question q) {
        require(voter, Privilege.CAST_DELETE_VOTE);
        boolean deleted = q.voteToDelete(voter, VOTES_TO_DELETE);
        log(voter.name() + " voted to delete question #" + q.id() + (deleted ? " -> DELETED" : ""));
    }

    // Moderator-only operations: the parameter type is the permission check.
    void close(Moderator m, Question q, ClosingRemark remark) { q.close(remark); log(m.name() + " (mod) closed question #" + q.id() + " as " + remark); }
    void reopen(Moderator m, Question q) { q.reopen(); log(m.name() + " (mod) reopened question #" + q.id()); }
    void delete(Moderator m, Question q) { q.delete(); log(m.name() + " (mod) deleted question #" + q.id()); }
    void undelete(Moderator m, Question q) { q.undelete(); log(m.name() + " (mod) undeleted question #" + q.id() + " -> " + q.status()); }

    // ---------- helpers ----------
    private void adjust(Member m, int delta, String reason) {
        if (delta == 0) return;
        int before = m.reputation();
        int after = m.changeReputation(delta);
        log(String.format("    rep %-6s %+d -> %d (%s)", m.name(), delta, after, reason));
        listeners.forEach(l -> l.onReputationChanged(m, before, after, reason));
    }

    private static void requireActive(Member m) {
        if (m.status() != AccountStatus.ACTIVE) throw new IllegalStateException(m.name() + " is " + m.status());
    }

    private static void require(Member m, Privilege p) {
        requireActive(m);
        if (!m.hasPrivilege(p)) {
            throw new IllegalStateException(m.name() + " needs " + p.minReputation + " reputation for " + p + " (has " + m.reputation() + ")");
        }
    }

    private static void log(String s) { System.out.println(s); }
}
