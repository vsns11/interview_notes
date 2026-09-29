package lld.ch11.observer;

final class DashboardUpdater implements BuildListener {
    boolean offline = false;

    @Override
    public void onBuildFinished(int n, String result) {
        if (offline) throw new IllegalStateException("dashboard is offline");
        System.out.println("  [dashboard] status tile -> " + result);
    }
}
