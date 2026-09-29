package lld.ch08.abstractfactory;

public class Main {

    /** Composition root: the ONE place that chooses the family. */
    static StorageFactory chooseFactory(String db) {
        return switch (db) {
            case "postgres" -> new PostgresFactory();
            case "mysql"    -> new MySqlFactory();
            case "fake"     -> new FakeStorageFactory();
            default -> throw new IllegalArgumentException("unknown db " + db);
        };
    }

    public static void main(String[] args) {
        String fromEnv = System.getenv().getOrDefault("DB", "postgres");
        for (String db : new String[] { fromEnv, "mysql", "fake" }) {
            System.out.println("== Using " + db + " ==");
            new ReportService(chooseFactory(db)).runReport();   // ReportService never changes
        }
    }
}
