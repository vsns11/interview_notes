package lld.ch11.di;

import java.util.ArrayList;
import java.util.List;

/** Test double: records instead of sending. Only possible because the dependency is injected. */
public final class RecordingSender implements MessageSender {
    public final List<String> sent = new ArrayList<>();
    public void send(String to, String text) { sent.add(to + "|" + text); }
}
