package lld.ch11.observer;

import java.math.BigDecimal;

public class Main {
    record OrderPlaced(String orderId, BigDecimal amount) {}
    record OrderCancelled(String orderId) {}

    public static void main(String[] args) {
        System.out.println("=== 1. Build pipeline: subscribe, unsubscribe, failing listener ===");
        BuildPipeline pipeline = new BuildPipeline();
        DashboardUpdater dashboard = new DashboardUpdater();
        pipeline.addListener(new SlackNotifier());
        pipeline.addListener(dashboard);
        pipeline.addListener(new MetricsRecorder());
        Runnable unsubscribeDeploy = pipeline.addListener(new DeployTrigger());

        pipeline.finish(101, "success");        // every listener runs
        unsubscribeDeploy.run();                // e.g. staging has no auto-deploy
        pipeline.finish(102, "success");        // three react
        dashboard.offline = true;
        pipeline.finish(103, "failed");         // dashboard throws, the others still run

        System.out.println("\n=== 2. Push vs pull ===");
        WeatherStation station = new WeatherStation();
        station.subscribePush(r -> System.out.println("  [push display] got everything: " + r));
        station.subscribePull(s -> System.out.println("  [pull display] asked only for temp: " + s.temperature() + " C"));
        station.measure(new WeatherStation.Reading(31.5, 0.62, 1008));

        System.out.println("\n=== 3. Event bus (typed topics, scoped unsubscribe) ===");
        EventBus bus = new EventBus();
        bus.subscribe(OrderPlaced.class, e -> System.out.println("  [email] confirmation for " + e.orderId()));
        bus.subscribe(Object.class, e -> System.out.println("  [audit] " + e));
        EventBus.Subscription refunds = bus.subscribe(OrderCancelled.class,
                e -> System.out.println("  [refunds] start refund for " + e.orderId()));
        try {
            bus.publish(new OrderPlaced("A-1", new BigDecimal("499.00")));
            bus.publish(new OrderCancelled("A-1"));
        } finally {
            refunds.close();                                   // refunds handler removed here, even on error
        }
        bus.publish(new OrderCancelled("A-2"));                // only audit sees this one

        System.out.println("\n=== 4. JDK: PropertyChangeSupport (replacement for deprecated Observable) ===");
        PricedItem phone = new PricedItem("phone", 49_900);
        phone.addPropertyChangeListener(evt -> System.out.println(
            "  [price watcher] " + phone.name() + " " + evt.getPropertyName() + ": "
            + evt.getOldValue() + " -> " + evt.getNewValue()));
        phone.setPriceCents(44_900);
        phone.setPriceCents(44_900);    // same value: no event fired
        phone.setPriceCents(39_900);
    }
}
