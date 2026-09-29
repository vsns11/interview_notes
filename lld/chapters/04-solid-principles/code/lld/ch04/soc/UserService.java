package lld.ch04.soc;

/** Business-rules concern only. Storage is somebody else's job. */
public class UserService {
    private final UserStore store;

    public UserService(UserStore store) {
        this.store = store;
    }

    boolean isValidUsername(String username) {
        return username.length() >= 3;
    }

    boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    public boolean registerUser(String username, String email) {
        if (!isValidUsername(username)) {
            System.out.println("Rejected: " + username + " is too short");
            return false;
        }
        if (!isValidEmail(email)) {
            System.out.println("Rejected: " + email + " is not an email address");
            return false;
        }
        store.addUser(username, email);
        return true;
    }
}
