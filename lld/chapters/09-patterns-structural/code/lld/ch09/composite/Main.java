package lld.ch09.composite;

public class Main {

    /** Client: one parameter type, no instanceof anywhere. */
    static void report(FileSystemNode node) {
        System.out.println("  " + node.name() + " -> " + node.size() + " bytes");
    }

    public static void main(String[] args) {
        System.out.println("== Safe composite: file system ==");
        FileNode readme = new FileNode("README.md", 1200);
        FolderNode src = new FolderNode("src")
                .add(new FileNode("main.js", 600))
                .add(new FileNode("util.js", 350));
        FolderNode project = new FolderNode("project").add(readme).add(src);

        project.print("  ");
        report(project);   // a whole tree
        report(readme);    // a single file - same call

        try {
            src.add(project);   // project already contains src -> would loop forever
        } catch (IllegalArgumentException e) {
            System.out.println("  rejected: " + e.getMessage());
        }

        System.out.println("== Transparent composite: restaurant menu ==");
        MenuComponent all = new Menu("All menus");
        MenuComponent breakfast = new Menu("Breakfast");
        MenuComponent desserts = new Menu("Desserts");
        breakfast.add(new MenuItem("Pancakes", "3.49", true));
        breakfast.add(new MenuItem("Bacon & eggs", "4.99", false));
        desserts.add(new MenuItem("Apple pie", "2.59", true));
        breakfast.add(desserts);          // a menu inside a menu
        all.add(breakfast);
        all.add(new MenuItem("Coffee", "1.50", true));

        all.print("  ");
        System.out.println("  total $" + all.price() + ", vegetarian items: " + all.countVegetarian());
        try {
            new MenuItem("Tea", "1.00", true).add(new MenuItem("Milk", "0.20", true));
        } catch (UnsupportedOperationException e) {
            System.out.println("  transparent cost: " + e.getMessage());
        }
    }
}
