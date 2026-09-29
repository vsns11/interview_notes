package lld.ch03.realization;

import java.util.List;

/**
 * AlertService is associated with the INTERFACE, not with any concrete class.
 * Adding a PushNotifier later needs no change here.
 */
public final class AlertService {
    private final List<Notifier> notifiers;

    public AlertService(List<Notifier> notifiers) { this.notifiers = List.copyOf(notifiers); }

    public void alert(String to, String message) {
        for (Notifier n : notifiers) {
            System.out.println("via " + n.channel());
            n.send(to, message);
        }
    }
}
