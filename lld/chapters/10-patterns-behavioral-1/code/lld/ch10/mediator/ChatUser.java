package lld.ch10.mediator;

final class ChatUser {
    private final String name;
    private ChatRoom room;

    ChatUser(String name) { this.name = name; }

    String name() { return name; }
    void joined(ChatRoom room) { this.room = room; }
    void say(String text) { room.send(this, null, text); }
    void whisper(String to, String text) { room.send(this, to, text); }
    void receive(String from, String text) { System.out.println("  " + name + " sees <" + from + "> " + text); }
}
