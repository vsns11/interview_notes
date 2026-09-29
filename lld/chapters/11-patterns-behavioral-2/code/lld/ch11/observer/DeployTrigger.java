package lld.ch11.observer;

final class DeployTrigger implements BuildListener {
    @Override
    public void onBuildFinished(int n, String result) {
        if (result.equals("success")) System.out.println("  [deploy] deploying build " + n);
    }
}
