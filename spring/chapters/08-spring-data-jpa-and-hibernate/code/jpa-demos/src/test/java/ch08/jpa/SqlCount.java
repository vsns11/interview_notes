package ch08.jpa;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import ch08.jpa.support.SqlRecorder;

/** Counts JDBC statements with Hibernate Statistics and prints their SQL (from SqlRecorder). */
final class SqlCount {

    private final Statistics stats;

    SqlCount(EntityManagerFactory emf) {
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
    }

    void reset() {
        stats.clear();
        SqlRecorder.clear();
    }

    long statements() {
        return stats.getPrepareStatementCount();
    }

    List<String> sql() {
        return SqlRecorder.statements();
    }

    void print(String label) {
        print(label, 190);
    }

    void print(String label, int maxChars) {
        System.out.println(label + " -> " + statements() + " SQL statement(s)");
        for (String s : SqlRecorder.statements()) {
            System.out.println("    " + (s.length() > maxChars ? s.substring(0, maxChars - 3) + "..." : s));
        }
    }
}
