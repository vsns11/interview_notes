package ch09.cache;

import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "products")      // default cache name for the annotations in this class
public class ProductService {

    private final ProductBackend backend;

    public ProductService(ProductBackend backend) { this.backend = backend; }

    /** Look in "products" under key sku; on a miss run the method and store the result. */
    @Cacheable(unless = "#result == null")          // do not cache "not found"
    public Product find(String sku) {
        return backend.load(sku);
    }

    /** ALWAYS runs the method, then writes the result into the cache (key = the saved product's sku). */
    @CachePut(key = "#result.sku()")
    public Product update(Product product) {
        return backend.store(product);
    }

    /** Runs the method, then removes that one key. */
    @CacheEvict
    public void delete(String sku) {
        backend.remove(sku);
    }

    /** Empties the whole "products" cache (e.g. after a bulk import). */
    @CacheEvict(allEntries = true)
    public void reloadAll() {}

    /** PITFALL demo: returns a MUTABLE list, and the cache hands every caller the same instance. */
    @Cacheable("tags")
    public List<String> tags(String sku) {
        return new ArrayList<>(backend.tags(sku));
    }
}
