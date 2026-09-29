package lld.ch08.abstractfactory;

/** Abstract product 1. */
interface Connection {
    String connect();
}

/** Abstract product 2. Must agree with the Connection about the SQL dialect. */
interface QueryBuilder {
    String selectAll(String table, int limit);
}

// ---- Postgres family ----
final class PostgresConnection implements Connection {
    public String connect() { return "connected to postgres://localhost:5432/app"; }
}
final class PostgresQueryBuilder implements QueryBuilder {
    public String selectAll(String table, int limit) {
        return "SELECT * FROM \"" + table + "\" LIMIT " + limit;      // Postgres quotes with double quotes
    }
}

// ---- MySQL family ----
final class MySqlConnection implements Connection {
    public String connect() { return "connected to mysql://localhost:3306/app"; }
}
final class MySqlQueryBuilder implements QueryBuilder {
    public String selectAll(String table, int limit) {
        return "SELECT * FROM `" + table + "` LIMIT " + limit;        // MySQL quotes with backticks
    }
}

// ---- Fake family for tests ----
final class InMemoryConnection implements Connection {
    public String connect() { return "connected to in-memory store (no network)"; }
}
final class RecordingQueryBuilder implements QueryBuilder {
    public String selectAll(String table, int limit) { return "[recorded] selectAll(" + table + ", " + limit + ")"; }
}
