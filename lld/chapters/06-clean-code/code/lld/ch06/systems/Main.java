package lld.ch06.systems;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Systems: separate construction from use, inject dependencies, handle cross-cutting concerns in one place. */
public class Main {

    interface AccountRepository { long balance(String id); void update(String id, long balance); }
    interface AccountService { long balance(String id); void deposit(String id, long amount); }

    static final class InMemoryAccountRepository implements AccountRepository {
        private final Map<String, Long> rows = new HashMap<>(Map.of("acc-1", 100L));
        public long balance(String id) { return rows.getOrDefault(id, 0L); }
        public void update(String id, long b) { rows.put(id, b); }
    }

    // ---------------- BEFORE: the service builds its own dependency (lazy init) and mixes in logging/timing ----------------
    static class Before {
        static final class AccountServiceImpl implements AccountService {
            private AccountRepository repo;
            private AccountRepository repo() {
                if (repo == null) repo = new InMemoryAccountRepository();   // construction mixed with use; hard-coded impl
                return repo;
            }
            public long balance(String id) {
                System.out.println("  LOG enter balance " + id);          // cross-cutting code copy #1
                long b = repo().balance(id);
                System.out.println("  LOG exit balance = " + b);
                return b;
            }
            public void deposit(String id, long amount) {
                System.out.println("  LOG enter deposit " + id);          // cross-cutting code copy #2
                if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
                repo().update(id, repo().balance(id) + amount);
                System.out.println("  LOG exit deposit");
            }
        }
    }

    // ---------------- AFTER ----------------
    static class After {
        /** Pure business logic. Gets its dependency from outside (constructor injection). No logging code. */
        static final class AccountServiceImpl implements AccountService {
            private final AccountRepository repo;
            AccountServiceImpl(AccountRepository repo) { this.repo = repo; }
            public long balance(String id) { return repo.balance(id); }
            public void deposit(String id, long amount) {
                if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
                repo.update(id, repo.balance(id) + amount);
            }
        }

        /** Cross-cutting concern written ONCE and applied to any interface via a JDK dynamic proxy (how Spring AOP works for interfaces). */
        @SuppressWarnings("unchecked")
        static <T> T withLogging(Class<T> type, T target) {
            InvocationHandler handler = (proxy, method, args) -> {
                String argText = args == null ? "" : String.join(", ", java.util.Arrays.stream(args).map(String::valueOf).toList());
                System.out.println("  LOG -> " + method.getName() + "(" + argText + ")");
                try {
                    Object result = method.invoke(target, args);
                    System.out.println("  LOG <- " + method.getName() + (result == null ? "" : " = " + result));
                    return result;
                } catch (InvocationTargetException e) {
                    System.out.println("  LOG !! " + method.getName() + " threw " + e.getCause().getMessage());
                    throw e.getCause();                         // rethrow the real exception, not the reflection wrapper
                }
            };
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        }

        /** Composition root: the ONLY place that knows concrete classes and wires them together (the "main" side). */
        static AccountService buildApplication() {
            AccountRepository repo = new InMemoryAccountRepository();
            AccountService core = new AccountServiceImpl(repo);
            return withLogging(AccountService.class, core);
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        var before = new Before.AccountServiceImpl();
        before.deposit("acc-1", 50);
        System.out.println("balance = " + before.balance("acc-1"));

        System.out.println("== AFTER ==");
        AccountService service = After.buildApplication();      // construction happens here, once
        service.deposit("acc-1", 50);                            // use happens everywhere else
        service.balance("acc-1");
        try { service.deposit("acc-1", -5); } catch (IllegalArgumentException e) { System.out.println("caller sees: " + e.getMessage()); }

        // In a unit test, construct the core with a different repository -- no logging, no real storage.
        var testService = new After.AccountServiceImpl(new InMemoryAccountRepository());
        testService.deposit("acc-1", 1);
        System.out.println("test double wiring works: " + testService.balance("acc-1"));
    }
}
