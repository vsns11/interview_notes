package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** A Java-based migration: for data changes that are awkward in SQL. Found by the class name V3__... */
public class V3__normalise_emails extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        var conn = context.getConnection();              // same connection and transaction as Flyway
        try (Statement select = conn.createStatement();
             ResultSet rs = select.executeQuery("SELECT id, email FROM customer");
             PreparedStatement update = conn.prepareStatement("UPDATE customer SET email = ? WHERE id = ?")) {
            while (rs.next()) {
                update.setString(1, rs.getString("email").trim().toLowerCase());
                update.setLong(2, rs.getLong("id"));
                update.addBatch();
            }
            update.executeBatch();
        }
    }
}
