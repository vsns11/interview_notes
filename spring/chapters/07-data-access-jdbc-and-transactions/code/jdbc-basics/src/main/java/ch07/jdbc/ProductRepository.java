package ch07.jdbc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access with JdbcClient (Spring Framework 6.1+): fluent, named or positional params. */
@Repository
public class ProductRepository {

    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.sql("SELECT id, sku, name, price, in_stock FROM product ORDER BY id")
                   .query(Product.class)          // maps columns to record components by name
                   .list();
    }

    public Optional<Product> findBySku(String sku) {
        return jdbc.sql("SELECT id, sku, name, price, in_stock FROM product WHERE sku = :sku")
                   .param("sku", sku)
                   .query(Product.class)
                   .optional();
    }

    public List<String> namesCheaperThan(BigDecimal max) {
        return jdbc.sql("SELECT name FROM product WHERE price < ? ORDER BY price")
                   .param(max)                     // positional parameter
                   .query(String.class)            // single column -> simple type
                   .list();
    }

    public long insert(String sku, String name, BigDecimal price, int stock) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO product (sku, name, price, in_stock) VALUES (:sku, :name, :price, :stock)")
            .param("sku", sku).param("name", name).param("price", price).param("stock", stock)
            .update(keys, "id");                  // ask the driver for the generated id column
        return keys.getKeyAs(Long.class);
    }

    public int changeStock(String sku, int delta) {
        return jdbc.sql("UPDATE product SET in_stock = in_stock + :d WHERE sku = :sku")
                   .param("d", delta).param("sku", sku)
                   .update();                     // returns the number of rows changed
    }
}
