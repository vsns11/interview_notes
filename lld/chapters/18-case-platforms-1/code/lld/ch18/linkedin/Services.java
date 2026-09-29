package lld.ch18.linkedin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

enum NotificationType { NEW_MESSAGE, CONNECTION_INVITATION, NEW_COMMENT, RECOMMENDATION_REQUEST }

/** Only the data differs between notification kinds, so the kind is a field, not a subclass. */
record Notification(NotificationType type, String text, long at) {}

/** Strategy for delivery: in-app, email, push... */
interface NotificationChannel { void deliver(Member to, Notification n); }

final class InAppChannel implements NotificationChannel {
    @Override public void deliver(Member to, Notification n) { to.notify(n); }
}

final class EmailChannel implements NotificationChannel {
    @Override public void deliver(Member to, Notification n) {
        System.out.println("    [email to " + to.email() + "] " + n.type() + ": " + n.text());
    }
}

final class NotificationService {
    private final List<NotificationChannel> channels = new CopyOnWriteArrayList<>();
    NotificationService(List<NotificationChannel> channels) { this.channels.addAll(channels); }
    void send(Member to, NotificationType type, String text) {
        Notification n = new Notification(type, text, Tick.next());
        channels.forEach(c -> c.deliver(to, n));
    }
}

interface Search {
    List<Member> searchMembers(String name);
    List<Company> searchCompanies(String name);
    List<JobPosting> searchJobs(String title);
}

/**
 * Inverted indexes. RULE: every key maps to a LIST, even for one match,
 * so the second "Sharma" never breaks the index.
 */
final class SearchIndex implements Search {
    private final Map<String, Set<Member>> members = new ConcurrentHashMap<>();
    private final Map<String, Set<Company>> companies = new ConcurrentHashMap<>();
    private final Map<String, Set<JobPosting>> jobs = new ConcurrentHashMap<>();

    void addMember(Member m) { words(m.name()).forEach(w -> members.computeIfAbsent(w, k -> ConcurrentHashMap.newKeySet()).add(m)); }
    void removeMember(Member m, String oldName) { words(oldName).forEach(w -> members.getOrDefault(w, Set.of()).remove(m)); }
    void addCompany(Company c) { words(c.name()).forEach(w -> companies.computeIfAbsent(w, k -> ConcurrentHashMap.newKeySet()).add(c)); }
    void addJob(JobPosting j) { words(j.title()).forEach(w -> jobs.computeIfAbsent(w, k -> ConcurrentHashMap.newKeySet()).add(j)); }

    @Override public List<Member> searchMembers(String q) {
        return match(members, q).sorted(Comparator.comparing(Member::name)).toList();
    }
    @Override public List<Company> searchCompanies(String q) {
        return match(companies, q).sorted(Comparator.comparing(Company::name)).toList();
    }
    @Override public List<JobPosting> searchJobs(String q) {
        return match(jobs, q).filter(JobPosting::isOpen).sorted(Comparator.comparingLong(JobPosting::id)).toList();
    }

    /** Items matching ALL words of the query. */
    private static <T> Stream<T> match(Map<String, Set<T>> index, String query) {
        List<String> ws = words(query);
        if (ws.isEmpty()) return Stream.empty();
        return index.getOrDefault(ws.get(0), Set.of()).stream()
                .filter(item -> ws.stream().allMatch(w -> index.getOrDefault(w, Set.of()).contains(item)));
    }

    private static List<String> words(String s) {
        return Arrays.stream(s.toLowerCase(Locale.ROOT).split("\\W+")).filter(w -> !w.isEmpty()).toList();
    }
}

/** Strategy for ordering the feed. */
interface FeedRanker {
    String name();
    Comparator<Post> order(Member viewer);
}

final class ChronologicalRanker implements FeedRanker {
    @Override public String name() { return "latest"; }
    @Override public Comparator<Post> order(Member viewer) {
        return Comparator.comparingLong(Post::createdAt).reversed();
    }
}

/** Engagement score with a simple age decay; 1st-degree connections get a boost over follows. */
final class EngagementRanker implements FeedRanker {
    @Override public String name() { return "top"; }
    @Override public Comparator<Post> order(Member viewer) {
        return Comparator.comparingDouble((Post p) -> -score(p, viewer)).thenComparing(Comparator.comparingLong(Post::createdAt).reversed());
    }
    private static double score(Post p, Member viewer) {
        double engagement = p.likeCount() + 2.0 * p.comments().size() + 3.0 * p.shares();
        double affinity = viewer.isConnectedTo(p.author()) ? 1.5 : 1.0;
        double age = Tick.now() - p.createdAt();
        return (1 + engagement) * affinity / (1 + age / 20.0);
    }
}

/**
 * Pull model ("fan-out on read"): build the feed when it is opened from the
 * posts of connections and followed members. Fine for small networks; see the
 * README for the push model and the hybrid real sites use.
 */
final class FeedService {
    private final java.util.function.LongFunction<Member> lookup;
    FeedService(java.util.function.LongFunction<Member> lookup) { this.lookup = lookup; }

    List<Post> feed(Member viewer, FeedRanker ranker, int limit) {
        Set<Long> sources = new java.util.HashSet<>(viewer.connections());
        sources.addAll(viewer.followingMembers());
        return sources.stream()
                .map(lookup::apply)
                .filter(m -> m.status() == AccountStatus.ACTIVE)
                .flatMap(m -> m.posts().stream())
                .sorted(ranker.order(viewer))
                .limit(limit)
                .toList();
    }
}
