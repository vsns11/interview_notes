package lld.ch11.repository;

public class Main {
    public static void main(String[] args) {
        UserRepository repo = new InMemoryUserRepository();   // swap for a JDBC version in production
        SignupService service = new SignupService(repo);
        System.out.println("  " + service.signUp("u1", "asha@example.com"));
        System.out.println("  " + service.signUp("u2", "ben@example.com"));
        System.out.println("  " + service.signUp("u3", "ASHA@example.com"));
        service.deactivate("u2");
        System.out.println("  active users: " + repo.findActive());
        System.out.println("  findById(u9): " + repo.findById("u9"));
        System.out.println("  deleteById(u2): " + repo.deleteById("u2"));
    }
}
