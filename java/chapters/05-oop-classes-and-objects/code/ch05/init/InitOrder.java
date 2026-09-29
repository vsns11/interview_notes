package ch05.init;

public class InitOrder {

    static class Parent {
        static { log("1. Parent static block"); }
        private final String p = log("3. Parent field initializer");
        { log("4. Parent instance block"); }
        Parent() { log("5. Parent constructor body"); }
    }

    static class Child extends Parent {
        static final String TAG = log("2. Child static field");
        private final String c = log("6. Child field initializer");
        { log("7. Child instance block"); }
        Child() {
            super();                                     // implicit if omitted
            log("8. Child constructor body");
        }
    }

    static String log(String s) { System.out.println(s); return s; }

    public static void main(String[] args) {
        System.out.println("--- first new Child() ---");
        new Child();
        System.out.println("--- second new Child(): static parts do not run again ---");
        new Child();
    }
}
