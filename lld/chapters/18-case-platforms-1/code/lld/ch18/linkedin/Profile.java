package lld.ch18.linkedin;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

enum EmploymentType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }

/** A deterministic logical clock so the demo output is repeatable (a real system uses Instant.now()). */
final class Tick {
    private static final AtomicLong NOW = new AtomicLong();
    static long next() { return NOW.incrementAndGet(); }
    static long now() { return NOW.get(); }
    private Tick() {}
}

record Experience(String title, String company, String location, YearMonth from, YearMonth to, EmploymentType type) {
    boolean isCurrent() { return to == null; }
}

record Education(String school, String degree, int fromYear, int toYear) {}

record Accomplishment(String title, String description) {}

/** A skill keeps the set of members who endorsed it, so nobody endorses twice. */
final class Skill {
    private final String name;
    private final Set<Long> endorsers = new LinkedHashSet<>();
    Skill(String name) { this.name = name; }
    String name() { return name; }
    synchronized boolean endorse(long memberId) { return endorsers.add(memberId); }
    synchronized int endorsements() { return endorsers.size(); }
}

enum RecommendationStatus { REQUESTED, WRITTEN, DECLINED }

/** Created EMPTY when requested; only the named author may fill it in, once. */
final class Recommendation {
    private final long authorId;
    private final String authorName;
    private RecommendationStatus status = RecommendationStatus.REQUESTED;
    private String text;

    Recommendation(long authorId, String authorName) { this.authorId = authorId; this.authorName = authorName; }

    synchronized void write(long byMemberId, String text) {
        if (byMemberId != authorId) throw new IllegalStateException("only " + authorName + " can write this recommendation");
        if (status != RecommendationStatus.REQUESTED) throw new IllegalStateException("recommendation is " + status);
        this.text = text;
        this.status = RecommendationStatus.WRITTEN;
    }

    long authorId() { return authorId; }
    String authorName() { return authorName; }
    synchronized RecommendationStatus status() { return status; }
    synchronized Optional<String> text() { return Optional.ofNullable(text); }
}

/**
 * Counters that are pushed in by whoever sees the event (a profile cannot
 * observe its own views or search appearances). The connection count is NOT
 * stored here: it is the size of the connection set, so it can never disagree.
 */
final class ProfileStats {
    private final AtomicInteger profileViews = new AtomicInteger();
    private final AtomicInteger searchAppearances = new AtomicInteger();
    void recordView() { profileViews.incrementAndGet(); }
    void recordSearchAppearance() { searchAppearances.incrementAndGet(); }
    int profileViews() { return profileViews.get(); }
    int searchAppearances() { return searchAppearances.get(); }
}

/** The profile owns all its parts (composition): delete the profile and they go too. */
final class Profile {
    private volatile String headline = "";
    private volatile String summary = "";
    private final List<Experience> experiences = new ArrayList<>();
    private final List<Education> educations = new ArrayList<>();
    private final Map<String, Skill> skills = new LinkedHashMap<>();
    private final List<Accomplishment> accomplishments = new ArrayList<>();
    private final List<Recommendation> recommendations = new ArrayList<>();
    private final ProfileStats stats = new ProfileStats();

    void setHeadline(String h) { headline = h; }
    void setSummary(String s) { summary = s; }
    String headline() { return headline; }
    String summary() { return summary; }

    synchronized void addExperience(Experience e) {
        if (e.to() != null && e.to().isBefore(e.from())) throw new IllegalArgumentException("experience ends before it starts");
        experiences.add(e);
    }
    synchronized void addEducation(Education e) { educations.add(e); }
    synchronized void addAccomplishment(Accomplishment a) { accomplishments.add(a); }
    synchronized Skill addSkill(String name) { return skills.computeIfAbsent(name, Skill::new); }
    synchronized Optional<Skill> skill(String name) { return Optional.ofNullable(skills.get(name)); }
    synchronized void addRecommendation(Recommendation r) { recommendations.add(r); }

    synchronized List<Experience> experiences() { return List.copyOf(experiences); }
    synchronized List<Education> educations() { return List.copyOf(educations); }
    synchronized List<Skill> skills() { return List.copyOf(skills.values()); }
    synchronized List<Recommendation> recommendations() { return List.copyOf(recommendations); }
    ProfileStats stats() { return stats; }

    synchronized Optional<Experience> currentPosition() {
        return experiences.stream().filter(Experience::isCurrent).findFirst();
    }
}
