package lld.ch11.observer;

final class MetricsRecorder implements BuildListener {
    private int recorded = 0;

    @Override
    public void onBuildFinished(int n, String result) {
        recorded++;
        System.out.println("  [metrics] recorded build " + n + " (total " + recorded + ")");
    }
}
