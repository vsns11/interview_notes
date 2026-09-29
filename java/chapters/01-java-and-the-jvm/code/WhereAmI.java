package ch01.jvm;

/** Asks the running JVM about itself. */
public class WhereAmI {
    public static void main(String[] args) {
        Runtime.Version v = Runtime.version();
        System.out.println("Feature release : " + v.feature());
        System.out.println("java.vendor     : " + System.getProperty("java.vendor"));
        System.out.println("java.vm.name    : " + System.getProperty("java.vm.name"));
        System.out.println("class file ver. : " + System.getProperty("java.class.version"));
        System.out.println("os.name         : " + System.getProperty("os.name"));
        System.out.println("CPU cores seen  : " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max heap (MiB)  : " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
    }
}
