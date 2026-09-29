package lld.ch09.bridge;

/**
 * ABSTRACTION: the high-level "what the message is" side.
 * The protected 'channel' field IS the bridge.
 */
abstract class Notification {
    protected final Channel channel;

    protected Notification(Channel channel) { this.channel = channel; }

    /** Builds the message, then hands it across the bridge. Never branches on channel type. */
    abstract void send();
}
