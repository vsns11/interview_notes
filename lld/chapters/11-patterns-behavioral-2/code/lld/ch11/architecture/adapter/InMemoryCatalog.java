package lld.ch11.architecture.adapter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import lld.ch11.architecture.port.ProductCatalog;

/** DRIVEN adapter: a fixed price list. */
public final class InMemoryCatalog implements ProductCatalog {
    private final Map<String, BigDecimal> prices;

    public InMemoryCatalog(Map<String, BigDecimal> prices) { this.prices = Map.copyOf(prices); }

    public Optional<BigDecimal> priceOf(String sku) { return Optional.ofNullable(prices.get(sku)); }
}
