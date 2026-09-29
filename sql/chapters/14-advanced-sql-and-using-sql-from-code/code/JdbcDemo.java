// tools_run: skip   (needs the PostgreSQL JDBC driver on the class path - run it with the command below)
//
// Chapter 14 - using PostgreSQL from Java with JDBC.
// Run from the repo root (Java 25; the jars are in .java-books/lib/):
//   bash tools_pg.sh start
//   java --enable-native-access=ALL-UNNAMED -cp '.java-books/lib/*' \
//        sql/chapters/14-advanced-sql-and-using-sql-from-code/code/JdbcDemo.java
//
// The book's private server listens only on a Unix socket (/tmp/.s.PGSQL.5544), so the URL adds the
// junixsocket socket factory. Against a normal TCP server the URL is simply
//   jdbc:postgresql://localhost:5544/ch_14_jdbc

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class JdbcDemo {

    static final String SOCKET = "?socketFactory=org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg"
                               + "&socketFactoryArg=/tmp/.s.PGSQL.5544";
    static final String SERVER = "jdbc:postgresql://localhost:5544/";
    static final String USER = "postgres", PASSWORD = "";   // the private server uses trust auth

    public static void main(String[] args) throws Exception {
        Path schema = Path.of("sql/chapters/14-advanced-sql-and-using-sql-from-code/code/00_schema.sql");

        // 1. A fresh database (CREATE DATABASE needs auto-commit, which is the JDBC default)
        try (Connection admin = DriverManager.getConnection(SERVER + "postgres" + SOCKET, USER, PASSWORD);
             Statement st = admin.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS ch_14_jdbc");
            st.execute("CREATE DATABASE ch_14_jdbc ENCODING 'UTF8' TEMPLATE template0");
        }

        try (Connection conn = DriverManager.getConnection(SERVER + "ch_14_jdbc" + SOCKET, USER, PASSWORD)) {
            DatabaseMetaData md = conn.getMetaData();
            System.out.println("connected: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion()
                    + " via " + md.getDriverName() + " " + md.getDriverVersion());
            try (Statement st = conn.createStatement()) {
                st.execute(Files.readString(schema));          // a script with no parameters
            }
            query(conn);
            injection(conn);
            transaction(conn);
            batch(conn);
        }
    }

    // 2. PreparedStatement: '?' placeholders, typed setters, typed getters
    static void query(Connection conn) throws SQLException {
        System.out.println("\n== PreparedStatement ==");
        String sql = """
                SELECT first_name, city, signup_date
                FROM customers WHERE country = ? AND signup_date >= ? ORDER BY customer_id""";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "India");
            ps.setObject(2, LocalDate.of(2025, 1, 1));        // java.time maps to DATE
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String city = rs.getString("city");         // SQL NULL -> Java null
                    LocalDate d = rs.getObject("signup_date", LocalDate.class);
                    System.out.printf("  %-6s %-10s %s%n", rs.getString(1), city == null ? "(null)" : city, d);
                }
            }
        }
    }

    // 3. SQL injection: concatenation vs a bound parameter
    static void injection(Connection conn) throws SQLException {
        System.out.println("\n== SQL injection ==");
        String evil = "nobody' OR '1'='1";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM customers WHERE last_name = '" + evil + "'")) {
            rs.next();
            System.out.println("concatenated : " + rs.getInt(1) + " rows");
        }
        try (PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM customers WHERE last_name = ?")) {
            ps.setString(1, evil);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("parameterized: " + rs.getInt(1) + " rows");
            }
        }
    }

    // 4. Transactions: turn auto-commit off, commit or roll back as one unit
    static void transaction(Connection conn) throws SQLException {
        System.out.println("\n== transaction ==");
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        try (PreparedStatement up = conn.prepareStatement(
                "UPDATE products SET unit_price = unit_price + ? WHERE product_id = ?")) {
            up.setBigDecimal(1, new BigDecimal("-10.00")); up.setInt(2, 5); up.executeUpdate();
            up.setBigDecimal(1, new BigDecimal("-99.00")); up.setInt(2, 2); up.executeUpdate(); // price < 0: CHECK fails
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();                                   // undo BOTH updates
            System.out.println("rolled back, SQLState " + e.getSQLState() + ": " + e.getMessage().lines().findFirst().orElse(""));
        } finally {
            conn.setAutoCommit(true);
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT unit_price FROM products WHERE product_id = 5")) {
            rs.next();
            System.out.println("desk price unchanged: " + rs.getBigDecimal(1));
        }
    }

    // 5. Batch: many parameter sets, one round trip; RETURNING for generated values
    static void batch(Connection conn) throws SQLException {
        System.out.println("\n== batch ==");
        List<String[]> newProducts = List.of(
                new String[]{"Monitor Arm", "Accessories", "59.00"},
                new String[]{"Webcam HD", "Electronics", "79.90"},
                new String[]{"Cable Tray", "Furniture", "19.50"});
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (product_id, name, category, unit_price) VALUES (?, ?, ?, ?)")) {
            int id = 100;
            for (String[] p : newProducts) {
                ps.setInt(1, id++);
                ps.setString(2, p[0]);
                ps.setString(3, p[1]);
                ps.setBigDecimal(4, new BigDecimal(p[2]));
                ps.addBatch();
            }
            int[] counts = ps.executeBatch();
            conn.commit();
            System.out.println("batch update counts: " + java.util.Arrays.toString(counts));
        } finally {
            conn.setAutoCommit(true);
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE products SET unit_price = round(unit_price * 1.05, 2) WHERE product_id >= ? RETURNING name, unit_price")) {
            ps.setInt(1, 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) System.out.println("  " + rs.getString(1) + " -> " + rs.getBigDecimal(2));
            }
        }
    }
}
