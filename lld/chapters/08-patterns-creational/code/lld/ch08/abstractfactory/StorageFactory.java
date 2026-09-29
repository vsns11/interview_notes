package lld.ch08.abstractfactory;

/** Abstract factory: one creation method per product in the family. */
public interface StorageFactory {
    Connection createConnection();
    QueryBuilder createQueryBuilder();
}

/** Concrete factories implement EVERY method and return only their own family (covariant return types). */
final class PostgresFactory implements StorageFactory {
    @Override public PostgresConnection createConnection()     { return new PostgresConnection(); }
    @Override public PostgresQueryBuilder createQueryBuilder() { return new PostgresQueryBuilder(); }
}

final class MySqlFactory implements StorageFactory {
    @Override public MySqlConnection createConnection()     { return new MySqlConnection(); }
    @Override public MySqlQueryBuilder createQueryBuilder() { return new MySqlQueryBuilder(); }
}

final class FakeStorageFactory implements StorageFactory {
    @Override public Connection createConnection()     { return new InMemoryConnection(); }
    @Override public QueryBuilder createQueryBuilder() { return new RecordingQueryBuilder(); }
}
