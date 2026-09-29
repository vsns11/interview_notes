package ch08.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class TryWithResources {

    static class Res implements AutoCloseable {
        private final String name;
        private final boolean failOnClose;
        Res(String name, boolean failOnClose) {
            this.name = name;
            this.failOnClose = failOnClose;
            System.out.println("open " + name);
        }
        void use(boolean fail) {
            System.out.println("use " + name);
            if (fail) throw new IllegalStateException("use of " + name + " failed");
        }
        @Override public void close() {
            System.out.println("close " + name);
            if (failOnClose) throw new IllegalStateException("close of " + name + " failed");
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("-- 1. normal: closed in reverse order");
        try (Res a = new Res("A", false); Res b = new Res("B", false)) {
            a.use(false);
            b.use(false);
        }

        System.out.println("-- 2. body fails: resources closed BEFORE catch runs");
        try (Res a = new Res("A", false)) {
            a.use(true);
        } catch (IllegalStateException e) {
            System.out.println("catch: " + e.getMessage());
        } finally {
            System.out.println("finally");
        }

        System.out.println("-- 3. body AND close fail: close exception is suppressed");
        try (Res a = new Res("A", true); Res b = new Res("B", true)) {
            b.use(true);
        } catch (IllegalStateException e) {
            System.out.println("primary: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) System.out.println("suppressed: " + s.getMessage());
        }

        System.out.println("-- 4. Java 9: effectively final variable as a resource");
        BufferedReader reader = new BufferedReader(new StringReader("line one\nline two"));
        try (reader) {
            System.out.println("first line: " + reader.readLine());
        }
        try {
            reader.readLine();
        } catch (IOException e) {
            System.out.println("after close: " + e.getMessage());
        }
    }
}
