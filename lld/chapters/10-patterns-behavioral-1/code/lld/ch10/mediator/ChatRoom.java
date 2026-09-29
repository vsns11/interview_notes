package lld.ch10.mediator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Chat mediator: users never hold references to each other, only to the room. */
final class ChatRoom {
    private final Map<String, ChatUser> members = new LinkedHashMap<>();
    private final Set<String> bannedWords = Set.of("spam");

    void join(ChatUser user) {
        members.put(user.name(), user);
        user.joined(this);
        broadcast("room", user.name() + " joined");
    }

    /** Room rules (moderation, routing) live here, not in the users. */
    void send(ChatUser from, String to, String text) {
        if (bannedWords.stream().anyMatch(text.toLowerCase()::contains)) {
            from.receive("room", "message blocked by moderation");
            return;
        }
        if (to == null) {
            members.values().stream().filter(u -> u != from).forEach(u -> u.receive(from.name(), text));
        } else if (members.containsKey(to)) {
            members.get(to).receive(from.name() + " (private)", text);
        } else {
            from.receive("room", to + " is not in the room");
        }
    }

    private void broadcast(String from, String text) {
        members.values().forEach(u -> u.receive(from, text));
    }
}
