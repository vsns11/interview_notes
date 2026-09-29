package ch16.classload;

/** Lazy loading, initialization order and the class-loader hierarchy. */
public class LoadingDemo {

    static class Config {                      // loaded + initialized only when first used
        static final String NAME = "compile-time constant"; // inlined by javac: does NOT trigger init
        static final long STARTED = System.nanoTime();       // not a constant: reading it DOES trigger init
        static { System.out.println("  [Config static initializer runs]"); }
    }

    static class Parent { static { System.out.println("  [Parent initialized]"); } static int shared = 1; }
    static class Child extends Parent { static { System.out.println("  [Child initialized]"); } }

    public static void main(String[] args) throws Exception {
        System.out.println("1. main started");
        System.out.println("2. reading Config.NAME   -> " + Config.NAME);
        System.out.println("3. reading Config.STARTED (triggers init):");
        long t = Config.STARTED;
        System.out.println("4. Child.shared via subclass name (only Parent initializes):");
        int s = Child.shared;
        System.out.println("5. new Child():");
        new Child();

        System.out.println("6. Who loaded what?");
        show("java.lang.String", String.class);
        show("java.sql.Date", Class.forName("java.sql.Date"));
        show("LoadingDemo", LoadingDemo.class);
        ClassLoader app = LoadingDemo.class.getClassLoader();
        System.out.println("   app loader's parent  : " + app.getParent().getName());
        System.out.println("   platform's parent    : " + app.getParent().getParent() + " (bootstrap is native)");
        System.out.println("   module of String     : " + String.class.getModule().getName());
        Module m = LoadingDemo.class.getModule();
        System.out.println("   module of LoadingDemo: " + (m.isNamed() ? m.getName() : "unnamed module (class path)"));
        if (t == 0 || s == 0) System.out.println("unreachable");
    }

    static void show(String label, Class<?> c) {
        ClassLoader cl = c.getClassLoader();
        System.out.printf("   %-17s loaded by %s%n", label, cl == null ? "bootstrap (null)" : cl.getName());
    }
}
