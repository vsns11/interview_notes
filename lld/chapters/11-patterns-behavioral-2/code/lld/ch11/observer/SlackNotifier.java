package lld.ch11.observer;

final class SlackNotifier implements BuildListener {
    @Override
    public void onBuildFinished(int n, String result) {
        System.out.println("  [slack] #builds: build " + n + " " + result);
    }
}
