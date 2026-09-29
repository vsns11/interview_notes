package ch08.jpa.support;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/** Hibernate calls inspect() with every SQL string it is about to prepare. We just remember them. */
public class SqlRecorder implements StatementInspector {

    private static final List<String> STATEMENTS = new ArrayList<>();

    @Override
    public synchronized String inspect(String sql) {
        STATEMENTS.add(sql);
        return sql; // unchanged
    }

    public static synchronized void clear() {
        STATEMENTS.clear();
    }

    public static synchronized List<String> statements() {
        return List.copyOf(STATEMENTS);
    }
}
