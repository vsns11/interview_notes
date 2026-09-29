package ch12.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("select p from Product p where p.price <= :max order by p.price")
    List<Product> cheaperThan(BigDecimal max);
}
