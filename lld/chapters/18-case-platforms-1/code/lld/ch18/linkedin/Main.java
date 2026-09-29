package lld.ch18.linkedin;

import java.time.YearMonth;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LinkedIn li = new LinkedIn(List.of(new InAppChannel(), new EmailChannel()));
        Member asha = li.join(new Member("Asha Sharma", "asha@example.com"));
        Member ravi = li.join(new Member("Ravi Sharma", "ravi@example.com"));
        Member meera = li.join(new Member("Meera Iyer", "meera@example.com"));
        Member karan = li.join(new Member("Karan Mehta", "karan@example.com"));
        Member zoya = li.join(new Member("Zoya Khan", "zoya@example.com"));
        Admin admin = li.join(new Admin("Site Admin", "admin@example.com"));

        section("1. Profile");
        Profile p = asha.profile();
        p.setHeadline("Backend engineer | Java | Distributed systems");
        p.addExperience(new Experience("Software Engineer", "Acme Cloud", "Bengaluru", YearMonth.of(2022, 7), null, EmploymentType.FULL_TIME));
        p.addEducation(new Education("IIT Madras", "B.Tech CSE", 2018, 2022));
        p.addSkill("Java");
        p.addSkill("System Design");
        attempt(() -> p.addExperience(new Experience("Intern", "X", "Pune", YearMonth.of(2021, 6), YearMonth.of(2021, 1), EmploymentType.INTERNSHIP)));
        System.out.println(asha + ": " + p.headline() + ", now at " + p.currentPosition().map(Experience::company).orElse("-"));

        section("2. Connections");
        ConnectionInvitation i1 = li.connections.invite(asha, meera, "We met at the Java meetup");
        li.connections.accept(i1, meera);
        attempt(() -> li.connections.accept(i1, meera));                 // accepted once, never again
        li.connections.accept(li.connections.invite(meera, karan, "Ex-colleague"), karan);
        li.connections.accept(li.connections.invite(karan, zoya, "Hi"), zoya);
        ConnectionInvitation pending = li.connections.invite(asha, ravi, "Same surname!");
        attempt(() -> li.connections.invite(ravi, asha, "Hello"));      // pending the other way
        attempt(() -> li.connections.accept(pending, asha));             // only the receiver can accept
        System.out.println("degree asha->meera=" + li.connections.degree(asha, meera).getAsInt()
                + ", asha->karan=" + li.connections.degree(asha, karan).getAsInt()
                + ", asha->zoya=" + li.connections.degree(asha, zoya).getAsInt()
                + ", asha->ravi=" + (li.connections.degree(asha, ravi).isPresent() ? "?" : "out of network"));
        System.out.println("people asha may know: " + li.connections.peopleYouMayKnow(asha, 5));

        section("3. Endorsements and recommendations");
        li.endorse(meera, asha, "Java");
        attempt(() -> li.endorse(meera, asha, "Java"));
        attempt(() -> li.endorse(karan, asha, "Java"));
        Recommendation rec = li.requestRecommendation(asha, meera);
        attempt(() -> rec.write(karan.id(), "Great engineer"));
        rec.write(meera.id(), "Asha owns problems end to end.");
        System.out.println("Java endorsements=" + p.skill("Java").orElseThrow().endorsements()
                + ", recommendation from " + rec.authorName() + ": " + rec.text().orElse("") + " [" + rec.status() + "]");

        section("4. Posts, comments, likes, feed");
        karan.follow(asha);                                              // follow = one-sided, no approval
        Post launch = li.post(meera, "We are hiring Java engineers at Acme Cloud!");
        Post tip = li.post(asha, "Tip: prefer composition over inheritance.");
        Comment c = li.comment(asha, launch, "Sharing with my network");
        launch.like(asha.id());
        c.like(meera.id());                                              // comments can be liked too
        System.out.println("double like accepted? " + launch.like(asha.id()));
        li.share(karan, tip, "Good advice");
        tip.like(meera.id());
        tip.like(zoya.id());
        System.out.println("feed(asha, latest): " + li.feeds.feed(asha, new ChronologicalRanker(), 5));
        System.out.println("feed(karan, top):   " + li.feeds.feed(karan, new EngagementRanker(), 5));

        section("5. Messaging");
        li.message(asha, List.of(meera), "Coffee on Friday?");
        attempt(() -> li.message(asha, List.of(karan), "Hi Karan"));

        section("6. Companies, jobs, groups");
        Company acme = li.createCompany(meera, "Acme Cloud");
        li.followCompany(asha, acme);
        JobPosting job = li.postJob(meera, acme, "Senior Java Engineer", "Bengaluru", EmploymentType.FULL_TIME);
        attempt(() -> li.postJob(asha, acme, "Fake job", "Nowhere", EmploymentType.CONTRACT));
        System.out.println("search jobs 'java engineer': " + li.search.searchJobs("java engineer"));
        job.apply(asha);
        attempt(() -> job.apply(asha));
        Group group = new Group("Java Developers India", meera);
        group.join(asha);
        attempt(() -> group.leave(meera));
        System.out.println(acme + " followers=" + acme.followerCount() + ", applicants=" + job.applicantCount()
                + ", group members=" + group.memberCount());

        section("7. Search and profile stats");
        System.out.println("search 'sharma' (by meera): " + li.searchMembers(meera, "sharma"));
        li.rename(zoya, "Zoya Ali");
        System.out.println("search 'khan' after rename: " + li.searchMembers(meera, "khan")
                + ", search 'ali': " + li.searchMembers(meera, "ali"));
        li.viewProfile(meera, asha);
        li.viewProfile(karan, asha);
        System.out.println("asha stats: views=" + p.stats().profileViews() + ", connections=" + asha.connections().size()
                + ", search appearances=" + p.stats().searchAppearances());
        System.out.println("meera's in-app notifications: " + meera.notifications().stream().map(Notification::type).toList());

        section("8. Admin");
        li.block(admin, karan);
        attempt(() -> li.post(karan, "spam spam"));
        System.out.println("feed(meera) hides blocked Karan's share: " + li.feeds.feed(meera, new ChronologicalRanker(), 5));
    }

    static void section(String t) { System.out.println("\n== " + t + " =="); }

    static void attempt(Runnable r) {
        try { r.run(); } catch (RuntimeException e) { System.out.println("  REJECTED: " + e.getMessage()); }
    }
}
