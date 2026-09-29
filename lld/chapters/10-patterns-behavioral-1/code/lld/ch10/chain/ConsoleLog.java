package lld.ch10.chain;

final class ConsoleLog extends LogHandler {
    ConsoleLog() { super(Level.DEBUG); }
    @Override protected void write(Level l, String m) { System.out.println("    console <- " + l + " " + m); }
}
