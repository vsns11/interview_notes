package ch08.practices;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BestPractices {

    record User(String name, String email) {
        User {
            Objects.requireNonNull(name, "name");                                  // fail fast
            if (email == null || !email.contains("@")) throw new IllegalArgumentException("bad email: " + email);
        }
    }

    static final Map<Integer, String> USERS = Map.of(1, "ada", 2, "linus");

    // Expected "not found": return Optional, not an exception
    static Optional<String> findUser(int id) { return Optional.ofNullable(USERS.get(id)); }

    // Precondition violated by the caller: throw an unchecked exception
    static String getUser(int id) {
        String u = USERS.get(id);
        if (u == null) throw new java.util.NoSuchElementException("no user with id " + id);
        return u;
    }

    public static void main(String[] args) {
        System.out.println("findUser(3) -> " + findUser(3).orElse("guest"));
        try {
            getUser(3);
        } catch (java.util.NoSuchElementException e) {
            System.out.println("getUser(3) -> " + e.getMessage());
        }

        try {
            new User(null, "a@b.c");
        } catch (NullPointerException e) {
            System.out.println("requireNonNull -> NPE: " + e.getMessage());
        }
        try {
            new User("ada", "not-an-email");
        } catch (IllegalArgumentException e) {
            System.out.println("validation -> " + e.getMessage());
        }

        // Helpful NullPointerException messages (Java 14+, on by default since 15)
        String[] names = new String[2];
        try {
            System.out.println(names[1].toUpperCase());
        } catch (NullPointerException e) {
            System.out.println("helpful NPE: " + e.getMessage());
        }

        // Exceptions are for exceptional cases, not control flow: a check is clearer and cheaper
        int[] data = {1, 2, 3};
        int i = 5;
        System.out.println("bounds check instead of catching: " + (i < data.length ? data[i] : "out of range"));

        // Never swallow silently. If you really must ignore, say why.
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();        // restore the interrupt flag
            System.out.println("interrupted");
        }
        System.out.println("done");
    }
}
