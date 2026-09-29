package lld.ch21.codingblox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Contest {
    private final int id;
    private final String name;
    private final Level level;
    private final String creator;
    private final Set<String> participants = new LinkedHashSet<>();
    private final List<ContestResult> results = new ArrayList<>();
    private ContestStatus status = ContestStatus.OPEN;

    Contest(int id, String name, Level level, String creator) {
        this.id = id; this.name = name; this.level = level; this.creator = creator;
        participants.add(creator);                       // the creator always attends
    }

    public int id() { return id; }
    public String name() { return name; }
    public Level level() { return level; }
    public String creator() { return creator; }
    public synchronized ContestStatus status() { return status; }
    public synchronized List<String> participants() { return List.copyOf(participants); }
    public synchronized List<ContestResult> results() { return List.copyOf(results); }

    synchronized void attend(String user) {
        requireOpen();
        if (!participants.add(user)) throw new BloxException(user + " already registered for contest " + id);
    }

    synchronized void withdraw(String user) {
        requireOpen();
        if (user.equals(creator)) throw new BloxException("Creator cannot withdraw from contest " + id);
        if (!participants.remove(user)) throw new BloxException(user + " is not in contest " + id);
    }

    synchronized void complete(List<ContestResult> r) {
        requireOpen();
        results.addAll(r);
        status = ContestStatus.COMPLETED;
    }

    private void requireOpen() {
        if (status != ContestStatus.OPEN) throw new BloxException("Contest " + id + " has already run");
    }

    @Override public String toString() { return id + " " + name + " " + level + " by " + creator + " [" + status() + "]"; }
}
