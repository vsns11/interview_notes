package ch05.constructors;

public class ConstructorsDemo {

    static class NoConstructorWritten {       // the compiler adds: NoConstructorWritten() { super(); }
        int x = 7;
    }

    static class OnlyArgConstructor {
        final int x;
        OnlyArgConstructor(int x) { this.x = x; }
        // No default constructor is generated now: new OnlyArgConstructor() would not compile.
    }

    public static void main(String[] args) {
        System.out.println(new Pizza());
        System.out.println(new Pizza("L"));
        System.out.println(new Pizza("S", true, "olives", "basil"));
        System.out.println(new Pizza(" m,ham,pineapple ", ','));
        System.out.println(new Pizza("M").addTopping("mushroom").addTopping("onion"));
        System.out.println("default ctor gives x=" + new NoConstructorWritten().x
                + ", arg ctor gives x=" + new OnlyArgConstructor(42).x);
        try {
            new Pizza("XXL");
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
