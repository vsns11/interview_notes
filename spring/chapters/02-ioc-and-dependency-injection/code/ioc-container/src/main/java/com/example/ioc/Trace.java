package com.example.ioc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** A tiny shared log so tests can show the ORDER in which Spring did things. */
public final class Trace {
    private static final List<String> LINES = new CopyOnWriteArrayList<>();
    private Trace() {}
    public static void add(String line) { LINES.add(line); }
    public static List<String> lines() { return List.copyOf(LINES); }
    public static void clear() { LINES.clear(); }
    public static void print(String title) {
        System.out.println("--- " + title + " ---");
        for (int i = 0; i < LINES.size(); i++) System.out.printf("%2d. %s%n", i + 1, LINES.get(i));
    }
}
