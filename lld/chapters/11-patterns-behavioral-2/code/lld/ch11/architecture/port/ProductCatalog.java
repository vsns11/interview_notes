package lld.ch11.architecture.port;

import java.math.BigDecimal;
import java.util.Optional;

/** DRIVEN port: prices come from the catalogue, never from the caller. */
public interface ProductCatalog {
    Optional<BigDecimal> priceOf(String sku);
}
