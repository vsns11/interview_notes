package ch06.overriding;

import java.io.IOException;

public class OverridingRules {

    static class Animal {
        protected Animal reproduce() { return new Animal(); }
        void speak() throws IOException { System.out.println("..."); }
        static String kind() { return "Animal (static)"; }
        String name = "animal field";
        final String dna() { return "ATGC"; }             // final: cannot be overridden
        public String toString() { return "Animal"; }
    }

    static class Dog extends Animal {
        @Override
        public Dog reproduce() { return new Dog(); }       // covariant return type + wider access: OK
        @Override
        void speak() { System.out.println("Woof"); }       // may throw fewer/narrower checked exceptions
        static String kind() { return "Dog (static)"; }    // HIDES Animal.kind(), does not override it
        String name = "dog field";                         // HIDES Animal.name
        // String dna() { ... }   -> compile error: dna() is final
        // private Dog reproduce() -> compile error: weaker access
        @Override public String toString() { return "Dog"; }
    }

    static class Base {
        Base() { init(); }                                 // calls an overridable method: dangerous
        void init() { }
    }
    static class Derived extends Base {
        private String label = "ready";
        @Override void init() { System.out.println("Derived.init sees label = " + label); }
    }

    public static void main(String[] args) throws IOException {
        Animal a = new Dog();                              // static type Animal, runtime type Dog
        a.speak();                                         // dynamic dispatch: Dog.speak
        System.out.println("reproduce() -> " + a.reproduce());
        System.out.println("a.kind()    -> " + a.kind() + "   (static method: picked by the declared type)");
        System.out.println("Dog.kind()  -> " + Dog.kind());
        System.out.println("a.name      -> " + a.name + " (fields use the STATIC type)");
        System.out.println("((Dog) a).name -> " + ((Dog) a).name);
        new Derived();                                     // override runs before Derived's fields are set
    }
}
