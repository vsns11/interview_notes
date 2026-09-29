package lld.ch11.repository;

/** Business logic talks to the repository interface only: no SQL, no maps, no files. */
public final class SignupService {
    private final UserRepository users;

    public SignupService(UserRepository users) { this.users = users; }

    public String signUp(String id, String email) {
        if (users.findByEmail(email).isPresent()) return "rejected: " + email + " already registered";
        users.save(new User(id, email, true));
        return "created " + id;
    }

    public void deactivate(String id) {
        users.findById(id).ifPresent(u -> users.save(new User(u.id(), u.email(), false)));
    }
}
