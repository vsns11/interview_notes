package ch04.proxy;

import java.util.List;
import org.springframework.stereotype.Service;

/** No interface -> CGLIB subclass proxy. Shows what happens to a final method. */
@Service
public class PricingService {

    private final List<String> currencies;

    public PricingService() {
        this.currencies = List.of("EUR", "USD");   // set in the constructor
    }

    public String currencies() {                   // normal method: proxy delegates to the real bean
        return "currencies=" + currencies;
    }

    public final String currenciesFinal() {        // final: the subclass proxy cannot override it
        return "currencies=" + currencies;
    }
}
