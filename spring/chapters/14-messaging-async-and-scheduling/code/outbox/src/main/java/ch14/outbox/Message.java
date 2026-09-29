package ch14.outbox;

import java.util.UUID;

/** What travels over the broker: a unique id, a key, a type and a JSON body. */
public record Message(UUID messageId, String key, String type, String payload) {}
