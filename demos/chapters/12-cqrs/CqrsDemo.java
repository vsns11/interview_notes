import java.util.*;
import java.util.concurrent.*;

/**
 * CQRS — runnable demo.
 *
 *   javac CqrsDemo.java && java CqrsDemo
 *
 * Shows:
 *   1. The formal rule: commands return nothing, queries change nothing
 *   2. Level 1 - two models, one store
 *   3. Level 2 - separate read store, and the CONSISTENCY WINDOW it creates
 *   4. Level 3 - event sourcing: rebuild the read model by replaying events
 *   5. Purpose-built read models: one write, several differently-shaped views
 */
public class CqrsDemo {

    // ================= COMMAND SIDE =================

    record CreateUser(String userId, String name, String email) {}
    record ChangeEmail(String userId, String newEmail) {}

    /** Events: the append-only history of what happened. */
    sealed interface UserEvent {
        String userId();
        record UserCreated(String userId, String name, String email) implements UserEvent {}
        record EmailChanged(String userId, String oldEmail, String newEmail) implements UserEvent {}
    }

    static class User {
        String id, name, email;
        User(String id, String name, String email) { this.id = id; this.name = name; this.email = email; }
    }

    static class EventStore {
        private final List<UserEvent> events = Collections.synchronizedList(new ArrayList<>());

        void append(UserEvent e) { events.add(e); }

        List<UserEvent> all() { return List.copyOf(events); }

        List<UserEvent> forUser(String userId) {
            return events.stream().filter(e -> e.userId().equals(userId)).toList();
        }
    }

    /** Command handlers: validate, mutate, emit an event. They return VOID. */
    static class UserCommandHandler {
        private final Map<String, User> writeStore = new ConcurrentHashMap<>();
        private final EventStore eventStore;
        private final List<java.util.function.Consumer<UserEvent>> projections = new ArrayList<>();

        UserCommandHandler(EventStore eventStore) { this.eventStore = eventStore; }

        void onEvent(java.util.function.Consumer<UserEvent> projection) {
            projections.add(projection);
        }

        void handle(CreateUser cmd) {                       // <- returns void
            if (cmd.email() == null || !cmd.email().contains("@")) {
                throw new IllegalArgumentException("invalid email");
            }
            writeStore.put(cmd.userId(), new User(cmd.userId(), cmd.name(), cmd.email()));
            publish(new UserEvent.UserCreated(cmd.userId(), cmd.name(), cmd.email()));
        }

        void handle(ChangeEmail cmd) {                      // <- returns void
            User u = writeStore.get(cmd.userId());
            if (u == null) throw new NoSuchElementException("no such user");
            if (!cmd.newEmail().contains("@")) throw new IllegalArgumentException("invalid email");
            String old = u.email;
            u.email = cmd.newEmail();
            publish(new UserEvent.EmailChanged(cmd.userId(), old, cmd.newEmail()));
        }

        private void publish(UserEvent e) {
            eventStore.append(e);
            projections.forEach(p -> p.accept(e));
        }

        User rawRead(String id) { return writeStore.get(id); }   // for demo comparison only
    }

    // ================= QUERY SIDE =================

    record UserProfileView(String id, String name, String email) {}
    record UserDirectoryEntry(String name, String initial) {}

    /** A read model, updated by consuming events. Can lag behind. */
    static class ProfileProjection {
        private final Map<String, UserProfileView> store = new ConcurrentHashMap<>();
        private final long lagMs;

        ProfileProjection(long lagMs) { this.lagMs = lagMs; }

        void apply(UserEvent e) {
            Runnable update = () -> {
                switch (e) {
                    case UserEvent.UserCreated c ->
                        store.put(c.userId(), new UserProfileView(c.userId(), c.name(), c.email()));
                    case UserEvent.EmailChanged c -> store.computeIfPresent(c.userId(),
                        (k, v) -> new UserProfileView(v.id(), v.name(), c.newEmail()));
                }
            };
            if (lagMs > 0) {
                CompletableFuture.runAsync(() -> { sleep(lagMs); update.run(); });
            } else {
                update.run();
            }
        }

        UserProfileView get(String id) { return store.get(id); }   // <- returns data, changes nothing
        void clear() { store.clear(); }
    }

    /** A DIFFERENT read model from the same events, shaped for a different screen. */
    static class DirectoryProjection {
        private final List<UserDirectoryEntry> entries = new CopyOnWriteArrayList<>();

        void apply(UserEvent e) {
            if (e instanceof UserEvent.UserCreated c) {
                entries.add(new UserDirectoryEntry(c.name(), c.name().substring(0, 1).toUpperCase()));
            }
        }

        List<UserDirectoryEntry> all() { return List.copyOf(entries); }
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws Exception {
        EventStore eventStore = new EventStore();
        UserCommandHandler commands = new UserCommandHandler(eventStore);

        System.out.println("\n=== 1. Level 1: two models, no lag ===");
        ProfileProjection fast = new ProfileProjection(0);
        commands.onEvent(fast::apply);

        commands.handle(new CreateUser("u1", "Alice", "alice@old.com"));
        System.out.println("  query -> " + fast.get("u1"));
        System.out.println("  >> command returned void; query returned data and changed nothing");

        System.out.println("\n=== 2. Validation lives on the command side ===");
        try {
            commands.handle(new CreateUser("u2", "Bob", "not-an-email"));
        } catch (IllegalArgumentException e) {
            System.out.println("  rejected: " + e.getMessage() + "  (read model never saw it)");
        }

        System.out.println("\n=== 3. Level 2: separate read store -> CONSISTENCY WINDOW ===");
        EventStore es2 = new EventStore();
        UserCommandHandler cmd2 = new UserCommandHandler(es2);
        ProfileProjection lagging = new ProfileProjection(250);   // 250ms replication lag
        cmd2.onEvent(lagging::apply);

        cmd2.handle(new CreateUser("u3", "Carol", "carol@old.com"));
        sleep(300);
        System.out.println("  before update: " + lagging.get("u3"));

        cmd2.handle(new ChangeEmail("u3", "carol@new.com"));
        System.out.println("  write store immediately: " + cmd2.rawRead("u3").email);
        System.out.println("  read model immediately:  " + lagging.get("u3").email()
                + "   <- STALE");
        sleep(300);
        System.out.println("  read model after 300ms:  " + lagging.get("u3").email()
                + "   <- converged");
        System.out.println("  >> this window is the central trade-off of CQRS");

        System.out.println("\n=== 4. Several read models from ONE write ===");
        DirectoryProjection directory = new DirectoryProjection();
        commands.onEvent(directory::apply);
        commands.handle(new CreateUser("u4", "Dave", "dave@x.com"));
        commands.handle(new CreateUser("u5", "Erin", "erin@x.com"));
        System.out.println("  profile view : " + fast.get("u4"));
        System.out.println("  directory view: " + directory.all());
        System.out.println("  >> same events, different shapes, each built for its screen");

        System.out.println("\n=== 5. Level 3: rebuild the read model by replaying events ===");
        System.out.println("  event log: ");
        eventStore.all().forEach(e -> System.out.println("      " + e));

        fast.clear();
        System.out.println("  after wiping the read model:  " + fast.get("u1"));
        eventStore.all().forEach(fast::apply);                 // replay
        System.out.println("  after replaying the log:      " + fast.get("u1"));
        System.out.println("  >> the write side is the source of truth; read models are disposable");
    }
}
