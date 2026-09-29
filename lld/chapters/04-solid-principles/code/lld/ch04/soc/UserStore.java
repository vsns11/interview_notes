package lld.ch04.soc;

import java.util.ArrayList;
import java.util.List;

/** Storage concern only. It has no opinion about what a valid user is. */
public class UserStore {
    public record User(String username, String email) { }

    private final List<User> users = new ArrayList<>();

    public void addUser(String username, String email) {
        users.add(new User(username, email));
        System.out.println("Stored user: " + username);
    }

    public int count() {
        return users.size();
    }
}
