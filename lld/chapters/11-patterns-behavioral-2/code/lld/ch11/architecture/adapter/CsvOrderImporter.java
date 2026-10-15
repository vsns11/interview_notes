package lld.ch11.architecture.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lld.ch11.architecture.pipeline.Filter;
import lld.ch11.architecture.pipeline.Result;
import lld.ch11.architecture.port.OrderingUseCase;
import lld.ch11.architecture.port.PlaceOrderCommand;

/**
 * DRIVING adapter: turns CSV lines ("customer, sku:qty, sku:qty") into calls on the
 * driving port. Parsing is a PIPE-AND-FILTER pipeline of four small filters.
 * Syntax checks live here; business rules (known products, payment) live in the core.
 */
public final class CsvOrderImporter {
    private static final Filter<String, PlaceOrderCommand> PIPELINE =
        Filter.of(CsvOrderImporter::dropBlankAndComments)
              .then(CsvOrderImporter::normalise)
              .then(CsvOrderImporter::splitFields)
              .then(CsvOrderImporter::toCommand);

    private final OrderingUseCase ordering;

    public CsvOrderImporter(OrderingUseCase ordering) { this.ordering = ordering; }

    public void importLines(List<String> lines) {
        for (int n = 1; n <= lines.size(); n++) {
            switch (PIPELINE.process(lines.get(n - 1))) {
                case Result.Rejected<PlaceOrderCommand> r ->
                    System.out.println("  line " + n + " dropped by filter: " + r.reason());
                case Result.Ok<PlaceOrderCommand> ok -> {
                    System.out.println("  line " + n + " -> " + ok.value());
                    try {
                        System.out.println("  line " + n + " receipt: " + ordering.place(ok.value()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("  line " + n + " rejected by core: " + e.getMessage());
                    }
                }
            }
        }
    }

    // ---- the four filters: each is a pure function, testable alone ----

    static Result<String> dropBlankAndComments(String line) {
        if (line.isBlank()) return new Result.Rejected<>("blank line");
        if (line.strip().startsWith("#")) return new Result.Rejected<>("comment");
        return new Result.Ok<>(line);
    }

    static Result<String> normalise(String line) {
        return new Result.Ok<>(line.strip().toLowerCase().replaceAll("\\s+", ""));
    }

    static Result<List<String>> splitFields(String line) {
        List<String> fields = Arrays.asList(line.split(","));
        return fields.size() < 2
            ? new Result.Rejected<>("expected a customer and at least one item in '" + line + "'")
            : new Result.Ok<>(fields);
    }

    static Result<PlaceOrderCommand> toCommand(List<String> fields) {
        List<PlaceOrderCommand.Item> items = new ArrayList<>();
        for (String token : fields.subList(1, fields.size())) {
            String[] parts = token.split(":");
            if (parts.length != 2 || !parts[1].matches("[1-9][0-9]{0,3}")) {
                return new Result.Rejected<>("bad item '" + token + "', expected sku:qty");
            }
            items.add(new PlaceOrderCommand.Item(parts[0], Integer.parseInt(parts[1])));
        }
        return new Result.Ok<>(new PlaceOrderCommand(fields.get(0), items));
    }
}
