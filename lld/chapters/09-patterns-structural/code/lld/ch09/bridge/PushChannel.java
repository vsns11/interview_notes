package lld.ch09.bridge;

final class PushChannel implements Channel {
    private final String deviceId;

    PushChannel(String deviceId) { this.deviceId = deviceId; }

    @Override
    public void deliver(String title, String body) {
        System.out.println("  [push to " + deviceId + "] (" + title + ") " + body);
    }
}
