package ch02.types;

/** The eight primitive types, their sizes and ranges, read from the wrapper classes. */
public class Primitives {
    public static void main(String[] args) {
        System.out.printf("%-8s %5s  %-22s %s%n", "type", "bits", "min", "max");
        System.out.printf("%-8s %5d  %-22d %d%n", "byte", Byte.SIZE, Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22d %d%n", "short", Short.SIZE, Short.MIN_VALUE, Short.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22d %d%n", "int", Integer.SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22d %d%n", "long", Long.SIZE, Long.MIN_VALUE, Long.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22d %d%n", "char", Character.SIZE, (int) Character.MIN_VALUE, (int) Character.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22s %s%n", "float", Float.SIZE, Float.MIN_VALUE, Float.MAX_VALUE);
        System.out.printf("%-8s %5d  %-22s %s%n", "double", Double.SIZE, Double.MIN_VALUE, Double.MAX_VALUE);
        System.out.println("boolean  true or false (size not specified by the language)");
    }
}
