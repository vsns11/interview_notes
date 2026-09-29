package lld.ch10.chain;

final class FileLog extends LogHandler {
    FileLog() { super(Level.INFO); }
    @Override protected void write(Level l, String m) { System.out.println("    app.log <- " + l + " " + m); }
}
