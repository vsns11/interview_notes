package lld.ch08.singleton;

/**
 * GoF note: a Singleton may be subclassed if the constructor is protected.
 * getInstance() decides ONCE, at run time, which subclass becomes "the" instance.
 */
public abstract class AppLogger {

    protected AppLogger() { }                              // protected, not private

    private static final class Holder {
        private static final AppLogger INSTANCE =
                "json".equals(System.getProperty("log.format")) ? new JsonLogger() : new PlainLogger();
    }

    public static AppLogger getInstance() { return Holder.INSTANCE; }

    public abstract void log(String message);
}

final class PlainLogger extends AppLogger {
    @Override public void log(String message) { System.out.println("  LOG " + message); }
}

final class JsonLogger extends AppLogger {
    @Override public void log(String message) { System.out.println("  {\"log\":\"" + message + "\"}"); }
}
