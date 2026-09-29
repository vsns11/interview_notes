package lld.ch10.chain;

final class PagerLog extends LogHandler {
    PagerLog() { super(Level.ERROR); }
    @Override protected void write(Level l, String m) { System.out.println("    PAGER   <- " + l + " " + m); }
}
