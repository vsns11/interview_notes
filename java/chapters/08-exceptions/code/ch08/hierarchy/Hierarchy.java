package ch08.hierarchy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Hierarchy {
    static String chain(Class<?> c) {
        List<String> names = new ArrayList<>();
        for (Class<?> k = c.getSuperclass(); k != Throwable.class; k = k.getSuperclass()) names.add(k.getSimpleName());
        return String.join(" > ", names) + " > Throwable";
    }

    static String kind(Class<? extends Throwable> c) {
        if (Error.class.isAssignableFrom(c)) return "error, unchecked";
        if (RuntimeException.class.isAssignableFrom(c)) return "unchecked";
        return "CHECKED";
    }

    public static void main(String[] args) {
        List<Class<? extends Throwable>> types = List.of(
                NullPointerException.class, IllegalArgumentException.class, NumberFormatException.class,
                ArrayIndexOutOfBoundsException.class, IllegalStateException.class, UnsupportedOperationException.class,
                ClassCastException.class, ArithmeticException.class, UncheckedIOException.class,
                IOException.class, FileNotFoundException.class, SQLException.class, InterruptedException.class,
                CloneNotSupportedException.class, StackOverflowError.class, OutOfMemoryError.class);
        for (Class<? extends Throwable> t : types) {
            System.out.printf("%s [%s]%n    extends %s%n", t.getSimpleName(), kind(t), chain(t));
        }
    }
}
