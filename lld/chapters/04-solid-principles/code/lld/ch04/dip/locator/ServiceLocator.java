package lld.ch04.dip.locator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/** A central registry that hands out services by name. */
public class ServiceLocator implements Locator {
    private final Map<String, PaymentService> services = new ConcurrentHashMap<>();

    public void register(String name, PaymentService service) {
        services.put(name, service);
    }

    @Override
    public PaymentService paymentService(String name) {
        PaymentService s = services.get(name);
        if (s == null) {
            throw new NoSuchElementException("No payment service named " + name);
        }
        return s;
    }
}
