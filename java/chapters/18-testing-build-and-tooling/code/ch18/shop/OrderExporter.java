package ch18.shop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writes orders as CSV — file I/O that tests check with a temporary directory. */
public final class OrderExporter {
    private OrderExporter() {}

    public static Path writeCsv(List<Order> orders, Path dir) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id,customer,total,placed_at");
        for (Order o : orders) lines.add(String.join(",", o.id(), o.customer(), o.total().toPlainString(), o.placedAt().toString()));
        return Files.write(dir.resolve("orders.csv"), lines);
    }
}
