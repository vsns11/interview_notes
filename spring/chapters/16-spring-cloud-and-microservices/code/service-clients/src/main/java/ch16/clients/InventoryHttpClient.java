package ch16.clients;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

// Spring Framework HTTP service interface (no extra library)
public interface InventoryHttpClient {

    @GetExchange("/stock/{sku}")
    Stock stock(@PathVariable String sku);
}
