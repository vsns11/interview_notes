package ch18.jdbc;

import ch18.shop.Order;
import ch18.shop.OrderRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** The production OrderRepository: plain JDBC. Only java.sql / javax.sql types — no driver class is named here. */
public final class JdbcOrderRepository implements OrderRepository {
    private final DataSource dataSource;

    public JdbcOrderRepository(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void save(Order o) {
        String sql = "INSERT INTO orders (id, customer, total, placed_at) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("save failed for " + o.id(), e);
        }
    }

    /** All or nothing: one transaction for the whole list. */
    public void saveAll(List<Order> orders) {
        String sql = "INSERT INTO orders (id, customer, total, placed_at) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);                       // start a transaction
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (Order o : orders) { bind(ps, o); ps.addBatch(); }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();                             // undo every insert of this call
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("saveAll failed", e);
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        List<Order> found = query("SELECT id, customer, total, placed_at FROM orders WHERE id = ?", id);
        return found.stream().findFirst();
    }

    @Override
    public List<Order> findByCustomer(String customer) {
        return query("SELECT id, customer, total, placed_at FROM orders WHERE customer = ? ORDER BY id", customer);
    }

    private List<Order> query(String sql, String param) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);                       // bound parameter: never concatenate user input
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RepositoryException("query failed: " + sql, e);
        }
    }

    private static void bind(PreparedStatement ps, Order o) throws SQLException {
        ps.setString(1, o.id());
        ps.setString(2, o.customer());
        ps.setBigDecimal(3, o.total());
        ps.setTimestamp(4, Timestamp.from(o.placedAt()));
    }

    private static Order map(ResultSet rs) throws SQLException {       // row -> record
        return new Order(rs.getString("id"), rs.getString("customer"),
                rs.getBigDecimal("total"), rs.getTimestamp("placed_at").toInstant());
    }

    /** Unchecked wrapper, so callers of the OrderRepository interface don't deal with SQLException. */
    public static final class RepositoryException extends RuntimeException {
        public RepositoryException(String message, SQLException cause) { super(message, cause); }
    }
}
