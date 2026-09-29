package lld.ch07.worked.step5;

import java.util.Locale;

/** Phase 2 abstraction. Extracted once a second renderer (HTML) was needed. */
public interface StatementRenderer {

    String render(StatementData data);

    static String money(double amount) {
        return String.format(Locale.ROOT, "%.2f", amount);
    }
}
