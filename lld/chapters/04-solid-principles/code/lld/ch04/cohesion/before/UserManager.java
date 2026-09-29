package lld.ch04.cohesion.before;

import java.util.ArrayList;
import java.util.List;

/** LOW cohesion: user bookkeeping and audit-file work share one class but not one purpose. */
public class UserManager {
    private final List<String> users = new ArrayList<>();
    private final List<String> auditLines = new ArrayList<>();

    // --- user work ---
    public void addUser(String user) {
        users.add(user);
        System.out.println("user added: " + user);
    }

    public void deleteUser(String user) {
        users.remove(user);
        System.out.println("user deleted: " + user);
    }

    // --- file work, unrelated to the two methods above ---
    public void writeToFile(String line) {
        auditLines.add(line);
        System.out.println("wrote a line to the audit file: " + line);
    }

    public List<String> readFromFile() {
        System.out.println("read " + auditLines.size() + " line(s) back from the audit file");
        return List.copyOf(auditLines);
    }
}
