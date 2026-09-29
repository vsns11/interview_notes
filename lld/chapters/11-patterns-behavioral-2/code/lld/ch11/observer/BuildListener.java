package lld.ch11.observer;

/** The OBSERVER interface: one method. A lambda can implement it. */
@FunctionalInterface
public interface BuildListener {
    void onBuildFinished(int buildNumber, String result);
}
