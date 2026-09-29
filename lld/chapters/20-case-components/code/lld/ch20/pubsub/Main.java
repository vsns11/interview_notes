package lld.ch20.pubsub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    /** Records what it received; can be told to fail some messages. */
    static final class RecordingSubscriber implements Subscriber {
        final List<String> received = Collections.synchronizedList(new ArrayList<>());
        final Set<Long> failOnceAt = ConcurrentHashMap.newKeySet();
        final String alwaysFailPayload;
        final long delayMs;

        RecordingSubscriber(long delayMs, String alwaysFailPayload) {
            this.delayMs = delayMs;
            this.alwaysFailPayload = alwaysFailPayload;
        }

        @Override
        public void onMessage(Message m) throws Exception {
            if (delayMs > 0) Thread.sleep(delayMs);
            if (m.payload().equals(alwaysFailPayload)) throw new Exception("cannot parse");
            if (failOnceAt.remove(m.offset())) throw new Exception("transient error");
            received.add(m.offset() + ":" + m.payload());
        }
    }

    public static void main(String[] args) throws Exception {
        try (Broker broker = new Broker()) {
            broker.createTopic("orders");

            RecordingSubscriber email = new RecordingSubscriber(0, null);
            RecordingSubscriber analytics = new RecordingSubscriber(20, null);       // slow consumer
            RecordingSubscriber inventory = new RecordingSubscriber(0, "BAD-JSON"); // poison message
            inventory.failOnceAt.add(1L);                                           // transient failure

            broker.subscribe("orders", "email", email, StartFrom.EARLIEST);
            broker.subscribe("orders", "analytics", analytics, StartFrom.EARLIEST);
            broker.subscribe("orders", "inventory", inventory, StartFrom.EARLIEST);

            System.out.println("== 1. Fan-out: every subscriber gets every message, in order");
            for (String p : List.of("order#1", "order#2", "BAD-JSON", "order#3"))
                broker.publish("orders", "k", p);
            System.out.println("  all acknowledged: " + broker.awaitIdle(5_000));
            System.out.println("  email     " + email.received);
            System.out.println("  analytics " + analytics.received + "  (slow, but did not slow the others)");
            System.out.println("  inventory " + inventory.received + "  (offset 1 retried, offset 2 dead-lettered)");
            Message dead = broker.topic(Broker.DLQ).awaitAt(0, 1_000);
            System.out.println("  dead letter: " + dead.payload());

            System.out.println("== 2. A late subscriber chooses where to start");
            RecordingSubscriber auditFromStart = new RecordingSubscriber(0, null);
            RecordingSubscriber onlyNew = new RecordingSubscriber(0, null);
            broker.subscribe("orders", "audit", auditFromStart, StartFrom.EARLIEST);
            Subscription latest = broker.subscribe("orders", "live", onlyNew, StartFrom.LATEST);
            broker.publish("orders", "k", "order#4");
            broker.awaitIdle(5_000);
            System.out.println("  audit (EARLIEST) " + auditFromStart.received);
            System.out.println("  live  (LATEST)   " + onlyNew.received);

            System.out.println("== 3. Replay: seek the 'live' subscription back to offset 3");
            latest.seek(3);
            broker.awaitIdle(5_000);
            System.out.println("  live after replay " + onlyNew.received + " (order#4 seen twice: at-least-once)");

            System.out.println("== 4. 4 publisher threads x 250 messages on a new topic");
            broker.createTopic("clicks");
            RecordingSubscriber clicks = new RecordingSubscriber(0, null);
            broker.subscribe("clicks", "counter", clicks, StartFrom.EARLIEST);
            List<Thread> pubs = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                int id = t;
                pubs.add(Thread.ofPlatform().start(() -> {
                    for (int i = 0; i < 250; i++) broker.publish("clicks", "user" + id, "c" + id + "-" + i);
                }));
            }
            for (Thread t : pubs) t.join();
            broker.awaitIdle(5_000);
            boolean inOrder = true;
            for (int i = 0; i < clicks.received.size(); i++)
                inOrder &= clicks.received.get(i).startsWith(i + ":");
            System.out.println("  received " + clicks.received.size() + " messages, offsets strictly in order: " + inOrder);
        }
    }
}
