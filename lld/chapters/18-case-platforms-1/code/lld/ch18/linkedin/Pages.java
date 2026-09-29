package lld.ch18.linkedin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

final class Company {
    private static final AtomicLong IDS = new AtomicLong(500);
    private final long id = IDS.incrementAndGet();
    private final String name;
    private final Member createdBy;
    private final Set<Long> followers = ConcurrentHashMap.newKeySet();
    private final List<JobPosting> jobs = new CopyOnWriteArrayList<>();     // owned: gone with the page

    Company(String name, Member createdBy) { this.name = name; this.createdBy = createdBy; }

    long id() { return id; }
    String name() { return name; }
    Member createdBy() { return createdBy; }
    void addFollower(long memberId) { followers.add(memberId); }
    Set<Long> followers() { return Set.copyOf(followers); }
    int followerCount() { return followers.size(); }          // counted, never stored

    JobPosting postJob(Member by, String title, String location, EmploymentType type) {
        if (by.id() != createdBy.id()) throw new IllegalStateException(by + " is not an admin of " + name);
        JobPosting job = new JobPosting(this, title, location, type);
        jobs.add(job);
        return job;
    }
    List<JobPosting> jobs() { return List.copyOf(jobs); }
    @Override public String toString() { return name; }
}

enum ApplicationStatus { APPLIED, IN_REVIEW, REJECTED, HIRED }

record JobApplication(Member applicant, long appliedAt, ApplicationStatus status) {}

final class JobPosting {
    private static final AtomicLong IDS = new AtomicLong(700);
    private final long id = IDS.incrementAndGet();
    private final Company company;
    private final String title;
    private final String location;
    private final EmploymentType type;
    private final Map<Long, JobApplication> applications = new LinkedHashMap<>();
    private volatile boolean open = true;

    JobPosting(Company company, String title, String location, EmploymentType type) {
        this.company = company; this.title = title; this.location = location; this.type = type;
    }

    long id() { return id; }
    Company company() { return company; }
    String title() { return title; }
    String location() { return location; }
    EmploymentType type() { return type; }
    boolean isOpen() { return open; }
    void close() { open = false; }

    synchronized JobApplication apply(Member m) {
        if (!open) throw new IllegalStateException("job " + title + " is closed");
        if (applications.containsKey(m.id())) throw new IllegalStateException(m + " already applied to " + title);
        JobApplication a = new JobApplication(m, Tick.next(), ApplicationStatus.APPLIED);
        applications.put(m.id(), a);
        return a;
    }
    synchronized int applicantCount() { return applications.size(); }
    @Override public String toString() { return title + " @ " + company + " (" + location + ", " + type + ")"; }
}

final class Group {
    private static final AtomicLong IDS = new AtomicLong(900);
    private final long id = IDS.incrementAndGet();
    private final String name;
    private final long ownerId;
    private final Set<Long> members = ConcurrentHashMap.newKeySet();

    Group(String name, Member owner) {
        this.name = name;
        this.ownerId = owner.id();
        members.add(owner.id());
    }

    long id() { return id; }
    String name() { return name; }
    boolean join(Member m) { return members.add(m.id()); }
    boolean leave(Member m) {
        if (m.id() == ownerId) throw new IllegalStateException("the owner cannot leave their own group");
        return members.remove(m.id());
    }
    int memberCount() { return members.size(); }
}
