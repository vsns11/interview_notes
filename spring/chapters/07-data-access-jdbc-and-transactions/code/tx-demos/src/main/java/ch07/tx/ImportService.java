package ch07.tx;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Proxy pitfalls: self-invocation, final methods, package-private methods. */
@Service
public class ImportService {

    private final Db db;

    public ImportService(Db db) {
        this.db = db;
    }

    /** NOT annotated. Calls saveAll() on "this" - the real object, not the proxy - so no transaction starts. */
    public void importAll(List<String> lines) {
        saveAll(lines);
    }

    @Transactional
    public void saveAll(List<String> lines) {
        for (String line : lines) {
            if (line.equals("BAD")) {
                throw new IllegalArgumentException("bad line");
            }
            db.audit(line);
        }
    }

    /** CGLIB cannot override a final method: the call runs on the proxy object itself, whose fields are null. */
    @Transactional
    public final String finalMethod() {
        return db.where();
    }

    /** Since Spring Framework 6.0, protected and package-private methods are transactional with class proxies. */
    @Transactional
    String packagePrivate() {
        return db.where();
    }
}
