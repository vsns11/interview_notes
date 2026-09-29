package lld.ch08.factorymethod;

import java.util.List;
import java.util.function.Supplier;

/** The modern, composition-based variant: pass the "factory method" in as a Supplier. */
public final class SupplierReportGenerator {
    private final Supplier<? extends Document> documentFactory;

    public SupplierReportGenerator(Supplier<? extends Document> documentFactory) {
        this.documentFactory = documentFactory;
    }

    public String generate(List<String> rows) {
        Document d = documentFactory.get();
        d.render(rows);
        return d.fileName();
    }
}
