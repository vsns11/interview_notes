package ch08.basics;

public class Propagation {
    static void level3(String s) {
        System.out.println("  level3 start");
        System.out.println("  length = " + s.length());   // NPE when s is null
        System.out.println("  level3 end");
    }
    static void level2(String s) {
        System.out.println(" level2 start");
        level3(s);
        System.out.println(" level2 end");
    }
    static void level1(String s) {
        System.out.println("level1 start");
        try {
            level2(s);
        } catch (NullPointerException e) {
            System.out.println("level1 caught: " + e.getMessage());
        }
        System.out.println("level1 end");
    }
    public static void main(String[] args) {
        level1("ok");
        System.out.println("---");
        level1(null);
    }
}
