package lld.ch02.bank;

/** Utility: formats whole cents as dollars. Private constructor = cannot be instantiated. */
public final class Money {
    private Money() { }
    public static String format(long cents) { return String.format("$%d.%02d", cents / 100, cents % 100); }
}
