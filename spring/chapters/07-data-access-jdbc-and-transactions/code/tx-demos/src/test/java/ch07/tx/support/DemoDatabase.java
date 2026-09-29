package ch07.tx.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Test plumbing (not Spring): before any test runs, create a fresh database spring_ch07_tx on the book's
 * private PostgreSQL; after the whole run, drop it. Registered in META-INF/services.
 */
public class DemoDatabase implements LauncherSessionListener {

    static final String ADMIN_URL = "jdbc:postgresql://127.0.0.1:5544/postgres";
    static final String NAME = "spring_ch07_tx";

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        run("DROP DATABASE IF EXISTS " + NAME + " WITH (FORCE)");
        run("CREATE DATABASE " + NAME);
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        run("DROP DATABASE IF EXISTS " + NAME + " WITH (FORCE)"); // FORCE: pool connections may still be open
    }

    private static void run(String sql) {
        try (Connection c = DriverManager.getConnection(ADMIN_URL, "postgres", "");
             Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Is the book's PostgreSQL running? bash tools_pg.sh start", e);
        }
    }
}
