package lld.ch08.singleton;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("== 1. Eager singleton: every caller shares one store ==");
        EagerConfigStore billing = EagerConfigStore.getInstance();
        EagerConfigStore reports = EagerConfigStore.getInstance();
        System.out.println("  billing reads region = " + billing.get("region"));
        reports.set("region", "eu-west-1");
        System.out.println("  after reports.set, billing reads = " + billing.get("region"));
        System.out.println("  same object? " + (billing == reports));

        System.out.println("\n== 2-4. Lazy variants hit by 8 threads at the same instant ==");
        race("naive lazy (unsafe)", NaiveLazySingleton::getInstance, NaiveLazySingleton.CONSTRUCTED::get);
        race("synchronized lazy", SynchronizedLazySingleton::getInstance, SynchronizedLazySingleton.CONSTRUCTED::get);
        race("double-checked locking", DclSingleton::getInstance, DclSingleton.CONSTRUCTED::get);

        System.out.println("\n== 5. Bill Pugh holder: nothing built until first use ==");
        System.out.println("  constructed so far: " + HolderSingleton.CONSTRUCTED.get());
        HolderSingleton h1 = HolderSingleton.getInstance();
        HolderSingleton h2 = HolderSingleton.getInstance();
        System.out.println("  constructed now: " + HolderSingleton.CONSTRUCTED.get() + ", same? " + (h1 == h2));

        System.out.println("\n== 6. Enum singleton ==");
        EnumConfigStore.INSTANCE.set("retries", "5");
        System.out.println("  retries = " + EnumConfigStore.INSTANCE.get("retries"));

        System.out.println("\n== 7. Breaking a naive singleton ==");
        attack("BreakableSingleton", BreakableSingleton.INSTANCE, BreakableSingleton.class);
        System.out.println("\n== 8. The hardened version ==");
        attack("HardenedSingleton", HardenedSingleton.INSTANCE, HardenedSingleton.class);
        System.out.println("\n== 9. Enum resists reflection and serialization ==");
        attackEnum();

        System.out.println("\n== 10. Subclassable singleton (protected constructor) ==");
        AppLogger.getInstance().log("chosen subclass = " + AppLogger.getInstance().getClass().getSimpleName());

        System.out.println("\n== 11. DI alternative: one instance created by the composition root ==");
        Config shared = new MapConfig(Map.of("region", "ap-south-1"));
        BillingService billingService = new BillingService(shared);
        ReportJob reportJob = new ReportJob(shared);
        System.out.println("  " + billingService.charge(499));
        System.out.println("  " + reportJob.run());
        Config testConfig = new MapConfig(Map.of("region", "test-region"));
        System.out.println("  in a test: " + new BillingService(testConfig).charge(1));
    }

    /** Starts 8 threads that all call getInstance() at the same moment. */
    private static void race(String label, Supplier<Object> getInstance, Supplier<Integer> constructed)
            throws Exception {
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Future<Object>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(pool.submit(() -> { start.await(); return getInstance.get(); }));
            }
            start.countDown();
            Set<Object> distinct = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Future<Object> f : results) distinct.add(f.get());
            System.out.printf("  %-24s constructor ran %d time(s), callers saw %d distinct object(s)%n",
                    label, constructed.get(), distinct.size());
        }
    }

    private static void attack(String name, Object instance, Class<?> type) {
        // Reflection
        try {
            Constructor<?> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            Object other = c.newInstance();
            System.out.println("  reflection    -> new object, same? " + (other == instance));
        } catch (ReflectiveOperationException | RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.out.println("  reflection    -> blocked: " + cause.getMessage());
        }
        // Serialization
        try {
            Object copy = roundTrip(instance);
            System.out.println("  serialization -> same? " + (copy == instance));
        } catch (Exception e) {
            System.out.println("  serialization -> failed: " + e);
        }
        // Cloning
        try {
            Object clone = ((CloneableBase) instance).clone();
            System.out.println("  clone         -> same? " + (clone == instance));
        } catch (CloneNotSupportedException e) {
            System.out.println("  clone         -> blocked: " + e.getMessage());
        }
    }

    private static void attackEnum() throws Exception {
        try {
            Constructor<?> c = EnumConfigStore.class.getDeclaredConstructors()[0];
            c.setAccessible(true);
            c.newInstance("SECOND", 1);
        } catch (IllegalArgumentException e) {
            System.out.println("  reflection    -> blocked: " + e.getMessage());
        }
        System.out.println("  serialization -> same? " + (roundTrip(EnumConfigStore.INSTANCE) == EnumConfigStore.INSTANCE));
    }

    private static Object roundTrip(Object o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(o); }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return in.readObject();
        }
    }
}
