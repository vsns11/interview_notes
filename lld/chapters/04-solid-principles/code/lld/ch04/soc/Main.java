package lld.ch04.soc;

public class Main {
    public static void main(String[] args) {
        UserStore store = new UserStore();
        UserService service = new UserService(store);
        service.registerUser("ada", "ada@example.com");
        service.registerUser("jo", "jo@example.com");
        service.registerUser("grace", "grace-at-example");
        System.out.println("Users in store: " + store.count());
    }
}
