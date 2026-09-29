package lld.ch21.codingblox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CodingBloxService {
    private final Repository<String, User> users = new Repository<>();
    private final Repository<Integer, Question> questions = new Repository<>();
    private final Repository<Integer, Contest> contests = new Repository<>();
    private final AtomicInteger questionSeq = new AtomicInteger();
    private final AtomicInteger contestSeq = new AtomicInteger();
    private final SolveSimulator simulator;

    public CodingBloxService(SolveSimulator simulator) { this.simulator = simulator; }

    public User createUser(String name) {
        User u = new User(name);
        if (!users.addIfAbsent(name, u)) throw new BloxException("User already exists: " + name);
        return u;
    }

    public Question createQuestion(Level level, int score) {
        if (score <= 0) throw new BloxException("Score must be positive");
        Question q = new Question(questionSeq.incrementAndGet(), level, score);
        questions.addIfAbsent(q.id(), q);
        return q;
    }

    public List<Question> listQuestions(Level levelOrNull) {
        return questions.all().stream().filter(q -> levelOrNull == null || q.level() == levelOrNull)
                .sorted(Comparator.comparingInt(Question::id)).toList();
    }

    public Contest createContest(String name, Level level, String creator) {
        user(creator);
        if (listQuestions(level).isEmpty()) throw new BloxException("No " + level + " questions to build a contest");
        Contest c = new Contest(contestSeq.incrementAndGet(), name, level, creator);
        contests.addIfAbsent(c.id(), c);
        return c;
    }

    public List<Contest> listContests(Level levelOrNull) {
        return contests.all().stream().filter(c -> levelOrNull == null || c.level() == levelOrNull)
                .sorted(Comparator.comparingInt(Contest::id)).toList();
    }

    public void attend(int contestId, String userName) { user(userName); contest(contestId).attend(userName); }

    public void withdraw(int contestId, String userName) { user(userName); contest(contestId).withdraw(userName); }

    /** Only the creator may run. Every participant gets random solved questions, then scores update. */
    public List<ContestResult> runContest(int contestId, String requester) {
        Contest c = contest(contestId);
        if (!c.creator().equals(requester)) throw new BloxException(requester + " did not create contest " + contestId);
        List<Question> pool = listQuestions(c.level());
        List<ContestResult> results = new ArrayList<>();
        for (String name : c.participants()) {
            List<Question> solved = simulator.solvedBy(name, pool);
            int points = solved.stream().mapToInt(Question::score).sum();
            results.add(new ContestResult(name, points, solved.stream().map(Question::id).toList()));
        }
        c.complete(results);                               // throws if already run: scores never double-apply
        for (ContestResult r : results) user(r.user()).applyContest(c.level(), r.points());
        return results;
    }

    public List<User> leaderboard(boolean descending) {
        Comparator<User> byScore = Comparator.comparingInt(User::score);
        if (descending) byScore = byScore.reversed();
        return users.all().stream().sorted(byScore.thenComparing(User::name)).toList();
    }

    public List<ContestResult> history(int contestId) {
        Contest c = contest(contestId);
        if (c.status() != ContestStatus.COMPLETED) throw new BloxException("Contest " + contestId + " has not run yet");
        return c.results().stream().sorted(Comparator.comparingInt(ContestResult::points).reversed()
                .thenComparing(ContestResult::user)).toList();
    }

    private User user(String n) { return users.find(n).orElseThrow(() -> new BloxException("No such user: " + n)); }
    private Contest contest(int id) { return contests.find(id).orElseThrow(() -> new BloxException("No such contest: " + id)); }
}
