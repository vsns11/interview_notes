package com.example.greet.internal;

public final class Formatter {                 // public, but its package is not exported
    public Formatter() {}
    public static String decorate(String s) { return "*** " + s + " ***"; }
}
