// tools_run: skip   (needs the book's private PostgreSQL — run it with the commands in section 18.11)
package ch18.jdbc;

import ch18.shop.Order;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Uses JdbcOrderRepository against the private PostgreSQL of the SQL Handbook (port 5544, Unix socket only).
 * Against an ordinary server the URL is just jdbc:postgresql://localhost:5432/shop.
 */
public class JdbcDemo {
    static final String SOCKET = "?socketFactory=org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg"
                               + "&socketFactoryArg=/tmp/.s.PGSQL.5544";
    static final String BASE = "jdbc:postgresql://localhost:5544/";

    public static void main(String[] args) throws SQLException {
        // 1. DriverManager: the simplest way to get one Connection (fine for scripts and admin tasks)
        try (Connection admin = DriverManager.getConnection(BASE + "postgres" + SOCKET, "postgres", "");
             Statement st = admin.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS ch_18_jdbc");
            st.execute("CREATE DATABASE ch_18_jdbc");
            System.out.println("driver: " + admin.getMetaData().getDriverName() + " " + admin.getMetaData().getDriverVersion());
        }

        // 2. DataSource: what applications use (a pool such as HikariCP implements the same interface)
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(BASE + "ch_18_jdbc" + SOCKET);
        ds.setUser("postgres");
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE orders (
                      id        text PRIMARY KEY,
                      customer  text NOT NULL,
                      total     numeric(10,2) NOT NULL CHECK (total >= 0),
                      placed_at timestamptz NOT NULL)""");
        }

        JdbcOrderRepository repo = new JdbcOrderRepository(ds);
        Instant t = Instant.parse("2025-09-16T10:00:00Z");
        repo.save(new Order("ORD-1", "ann", new BigDecimal("76.88"), t));
        repo.saveAll(List.of(new Order("ORD-2", "bob", new BigDecimal("12.50"), t.plusSeconds(60)),
                             new Order("ORD-3", "ann", new BigDecimal("9.99"), t.plusSeconds(120))));

        System.out.println("findById(ORD-1)   = " + repo.findById("ORD-1").orElseThrow());
        System.out.println("findById(ORD-404) = " + repo.findById("ORD-404"));
        System.out.println("ann's orders      = " + repo.findByCustomer("ann").stream().map(Order::id).toList());

        // 3. A failing transaction: ORD-4 is fine, ORD-1 is a duplicate key -> the whole batch is rolled back
        try {
            repo.saveAll(List.of(new Order("ORD-4", "cy", new BigDecimal("5.00"), t),
                                 new Order("ORD-1", "cy", new BigDecimal("1.00"), t)));
        } catch (JdbcOrderRepository.RepositoryException e) {
            SQLException cause = (SQLException) e.getCause();
            System.out.println(e.getMessage() + ": SQLState " + cause.getSQLState()
                    + " (" + cause.getClass().getSimpleName() + ")");
        }
        System.out.println("ORD-4 present after rollback? " + repo.findById("ORD-4").isPresent());
    }
}
