package lld.ch09.bridge;

final class SmsChannel implements Channel {
    private static final int LIMIT = 40;           // tiny limit so the demo shows truncation
    private final String phone;

    SmsChannel(String phone) { this.phone = phone; }

    @Override
    public void deliver(String title, String body) {
        String text = title + ": " + body;
        if (text.length() > LIMIT) text = text.substring(0, LIMIT - 3) + "...";
        System.out.println("  [sms to " + phone + "] " + text);
    }
}
