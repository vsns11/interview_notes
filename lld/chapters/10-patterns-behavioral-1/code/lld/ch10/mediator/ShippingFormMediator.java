package lld.ch10.mediator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** The ONLY place that knows how a country change affects methods, price and submit. */
final class ShippingFormMediator implements Mediator {
    private static final Map<String, Map<String, BigDecimal>> RATES = Map.of(
        "Germany", ordered("standard", "5.00", "express", "15.00"),
        "USA", ordered("standard", "8.00", "express", "20.00"),
        "Antarctica", Map.of());

    private final CountrySelect country;
    private final MethodSelect methods;
    private final PriceLabel price;
    private final SubmitButton submit;

    ShippingFormMediator(CountrySelect c, MethodSelect m, PriceLabel p, SubmitButton s) {
        this.country = c; this.methods = m; this.price = p; this.submit = s;
        for (Component comp : new Component[] {c, m, p, s}) comp.setMediator(this);
        submit.setEnabled(false);
    }

    @Override
    public void notify(Component sender, String event) {
        if (sender == country) {
            Map<String, BigDecimal> available = RATES.getOrDefault(event, Map.of());
            methods.setOptions(new ArrayList<>(available.keySet()));
            if (available.isEmpty()) {
                price.show("shipping not available");
                submit.setEnabled(false);
            } else {
                showPrice();                           // ORDER matters: reload methods first, then price
                submit.setEnabled(true);
            }
        } else if (sender == methods) {
            showPrice();
        } else if (sender == submit) {
            System.out.println("  [mediator] order placed: " + country.selected() + " / " + methods.selected());
        }
    }

    private void showPrice() {
        BigDecimal p = RATES.get(country.selected()).get(methods.selected());   // money: BigDecimal, never double
        price.show(methods.selected() + " shipping " + p.toPlainString());
    }

    private static Map<String, BigDecimal> ordered(String k1, String v1, String k2, String v2) {
        Map<String, BigDecimal> m = new LinkedHashMap<>();   // keeps "standard" before "express"
        m.put(k1, new BigDecimal(v1));
        m.put(k2, new BigDecimal(v2));
        return m;
    }
}
