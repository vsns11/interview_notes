package com.example.greet;

import com.example.greet.internal.Formatter;

public final class Greeter {
    public static String greet(String name) { return Formatter.decorate("Hello, " + name); }
}
