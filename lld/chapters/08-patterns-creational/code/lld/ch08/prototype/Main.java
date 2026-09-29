package lld.ch08.prototype;

public class Main {
    public static void main(String[] args) {
        System.out.println("== Copy instead of re-parsing ==");
        NotificationTemplate prototype = new NotificationTemplate("Welcome", "Hello there");
        NotificationTemplate ada = prototype.copy().withSubject("Welcome, Ada").addChannel("push");
        NotificationTemplate grace = prototype.copy().withSubject("Welcome, Grace").addChannel("sms");
        System.out.println("  ada       : " + ada);
        System.out.println("  grace     : " + grace);
        System.out.println("  prototype : " + prototype + "   <- unchanged (deep copy of the list)");
        System.out.println("  templates parsed: " + NotificationTemplate.parses);

        System.out.println("\n== Cloneable pitfall: shallow copy shares the list ==");
        ShallowPlaylist p1 = new ShallowPlaylist();
        p1.name = "Road trip"; p1.songs.add("Song A");
        ShallowPlaylist p2 = p1.clone();
        p2.songs.add("Song B");
        System.out.println("  original songs after editing the CLONE: " + p1.songs + "   <- bug");

        DeepPlaylist d1 = new DeepPlaylist();
        d1.name = "Road trip"; d1.songs.add("Song A");
        DeepPlaylist d2 = d1.clone();
        d2.songs.add("Song B");
        System.out.println("  deep clone -> original " + d1.songs + ", copy " + d2.songs);

        System.out.println("\n== Polymorphic copy: the caller only knows 'Shape' ==");
        Shape unknown = args.length > 0 ? new Rectangle(2, 3, "green") : new Circle(5, "red");
        Shape twin = unknown.copy();
        System.out.println("  copied a " + twin.getClass().getSimpleName() + ": " + twin.describe()
                + ", same object? " + (twin == unknown));

        System.out.println("\n== Prototype registry ==");
        ShapeRegistry registry = new ShapeRegistry();
        registry.register("button-bg", new Rectangle(120, 40, "blue"));
        registry.register("avatar", new Circle(24, "grey"));
        registry.register("badge", new Group().add(new Circle(8, "gold")).add(new Rectangle(30, 10, "gold")));
        Shape b1 = registry.create("badge");
        Shape b2 = registry.create("badge");
        System.out.println("  " + registry.create("button-bg").describe());
        System.out.println("  " + registry.create("avatar").describe());
        System.out.println("  " + b1.describe() + ", two badges distinct? " + (b1 != b2));
    }
}
