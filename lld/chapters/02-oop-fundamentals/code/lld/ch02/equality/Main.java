package lld.ch02.equality;

import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Point a = new Point(1, 2), b = new Point(1, 2);
        System.out.println("a == b (identity): " + (a == b));
        System.out.println("a.equals(b) (equality): " + a.equals(b));
        System.out.println("hash codes equal: " + (a.hashCode() == b.hashCode()));
        Set<Point> good = new HashSet<>();
        good.add(a); good.add(b);
        System.out.println("HashSet<Point> size after adding a and b: " + good.size() + ", contains (1,2)? " + good.contains(new Point(1, 2)));

        BrokenPoint x = new BrokenPoint(1, 2), y = new BrokenPoint(1, 2);
        Set<BrokenPoint> bad = new HashSet<>();
        bad.add(x);
        System.out.println("BrokenPoint equals: " + x.equals(y) + ", but set.contains(equal one): " + bad.contains(y));

        String s1 = "hi", s2 = new String("hi");
        System.out.println("\"hi\" == new String(\"hi\"): " + (s1 == s2) + ", equals: " + s1.equals(s2));

        Integer i1 = 1000, i2 = 1000;
        System.out.println("Integer 1000 equals: " + i1.equals(i2) + "  (never compare boxed numbers with ==)");
    }
}
