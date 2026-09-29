package ch07.jdbc;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** The same kind of access with the classic JdbcTemplate API, plus a batch update. */
@Repository
public class ProductTemplateDao {

    private static final RowMapper<Product> MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"), rs.getString("sku"), rs.getString("name"),
            rs.getBigDecimal("price"), rs.getInt("in_stock"));

    private final JdbcTemplate jdbc;

    public ProductTemplateDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM product", Integer.class);
    }

    public List<Product> inStockAtLeast(int min) {
        return jdbc.query("SELECT * FROM product WHERE in_stock >= ? ORDER BY sku", MAPPER, min);
    }

    /** Sends all rows in one JDBC batch: one round trip per batch instead of one per row. */
    public int[] restock(List<String> skus, int amount) {
        return jdbc.batchUpdate("UPDATE product SET in_stock = in_stock + ? WHERE sku = ?",
                skus.stream().map(s -> new Object[] {amount, s}).toList());
    }
}
