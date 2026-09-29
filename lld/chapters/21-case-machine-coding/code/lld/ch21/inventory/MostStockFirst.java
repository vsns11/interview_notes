package lld.ch21.inventory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Fewest split shipments: take from the fullest warehouse first. */
public class MostStockFirst implements AllocationStrategy {
    @Override public List<String> order(Map<String, Integer> stock, Map<String, Warehouse> warehouses) {
        return stock.keySet().stream()
                .sorted(Comparator.comparing((String w) -> stock.get(w)).reversed().thenComparing(w -> w))
                .toList();
    }
}
