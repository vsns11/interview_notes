package ch09.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** Key-generation demo: two methods share one cache and have the same parameter. */
@Service
public class CatalogService {

    @Cacheable("catalog")                             // key = sku  (default SimpleKeyGenerator)
    public String name(String sku) {
        return "name of " + sku;
    }

    @Cacheable("catalog")                             // SAME key = sku -> collides with name()!
    public String description(String sku) {
        return "description of " + sku;
    }

    @Cacheable(cacheNames = "catalog", key = "'desc:' + #sku")   // fix: make the key unique
    public String descriptionFixed(String sku) {
        return "description of " + sku;
    }

    @Cacheable(cacheNames = "catalog")                 // two parameters -> key = SimpleKey[sku, currency]
    public String price(String sku, String currency) {
        return "price of " + sku + " in " + currency;
    }

    @Cacheable(cacheNames = "catalog", key = "#sku", condition = "#currency == 'EUR'")
    public String priceEurOnly(String sku, String currency) {   // condition: cache only EUR lookups
        return "price of " + sku + " in " + currency;
    }
}
