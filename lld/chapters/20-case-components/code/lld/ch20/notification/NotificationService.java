package lld.ch20.notification;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** The class product code calls. It orders the steps and decides nothing itself. */
final class NotificationService implements AutoCloseable {
    private final Map<ChannelType, NotificationChannel> channels = new EnumMap<>(ChannelType.class);
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Preferences> preferences = new ConcurrentHashMap<>();
    private final Map<String, Template> templates = new ConcurrentHashMap<>();
    private final List<SendRule> rules;
    private final List<DeliveryListener> listeners = new CopyOnWriteArrayList<>();
    private final List<DeliveryRecord> history = new CopyOnWriteArrayList<>();
    private final TimeSource time;
    private final ExecutorService workers = Executors.newFixedThreadPool(4);

    NotificationService(List<NotificationChannel> channelList, List<SendRule> rules, TimeSource time) {
        channelList.forEach(c -> channels.put(c.type(), c));
        this.rules = List.copyOf(rules);
        this.time = time;
    }

    void register(User u, Preferences p) { users.put(u.id(), u); preferences.put(u.id(), p); }
    void addTemplate(Template t) { templates.put(t.kind(), t); }
    void subscribe(DeliveryListener l) { listeners.add(l); }
    List<DeliveryRecord> history() { return List.copyOf(history); }

    /** Synchronous send: rules -> render -> try each accepted channel in the user's order. */
    DeliveryRecord send(NotificationRequest req) {
        User user = users.get(req.userId());
        if (user == null) return finish(req, DeliveryStatus.FAILED, null, 0, "unknown user");
        Preferences prefs = preferences.get(req.userId());
        LocalDateTime now = time.now();

        for (SendRule rule : rules) {
            Optional<String> reason = rule.blocks(req, prefs, now);
            if (reason.isPresent()) return finish(req, DeliveryStatus.SUPPRESSED, null, 0, reason.get());
        }
        List<ChannelType> accepted = prefs.channelsFor(req.kind());
        if (accepted.isEmpty()) return finish(req, DeliveryStatus.SUPPRESSED, null, 0, "user accepts no channel");

        Template template = templates.get(req.kind());
        if (template == null) return finish(req, DeliveryStatus.FAILED, null, 0, "no template");
        Message message;
        try {
            message = template.render(req.values()); // rendered ONCE, same text on every channel
        } catch (IllegalArgumentException e) {        // e.g. a {hole} with no value: still leave via finish()
            return finish(req, DeliveryStatus.FAILED, null, 0, "template: " + e.getMessage());
        }

        int totalAttempts = 0;
        String lastError = null;
        for (ChannelType type : accepted) {
            NotificationChannel channel = channels.get(type);
            if (channel == null) continue;
            Attempt a = channel.deliver(user, message);
            totalAttempts += a.attempts();
            if (a.success()) {
                rules.forEach(r -> r.onSent(req, now));
                return finish(req, DeliveryStatus.SENT, type, totalAttempts, null);
            }
            lastError = type + ": " + a.error();
        }
        return finish(req, DeliveryStatus.FAILED, null, totalAttempts, "all channels failed, last " + lastError);
    }

    /** Asynchronous send: the caller (e.g. checkout) is not blocked by a slow provider. */
    CompletableFuture<DeliveryRecord> sendAsync(NotificationRequest req) {
        return CompletableFuture.supplyAsync(() -> send(req), workers);
    }

    /** The single exit: every outcome is stored and announced here. */
    private DeliveryRecord finish(NotificationRequest req, DeliveryStatus status, ChannelType channel,
                                  int attempts, String reason) {
        DeliveryRecord record = new DeliveryRecord(req.kind(), req.userId(), status, channel, attempts, reason);
        history.add(record);
        for (DeliveryListener l : listeners) {
            try {
                l.onOutcome(record);
            } catch (RuntimeException e) {
                // a broken listener must not break delivery
            }
        }
        return record;
    }

    @Override
    public void close() { workers.shutdown(); }
}
