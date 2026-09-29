package lld.ch21.inventory;

/** reorderPoint: raise an alert when total stock falls to this level or below. */
public record Product(String sku, String name, int reorderPoint, int maxLevel) {
    public Product {
        if (reorderPoint < 0 || maxLevel <= reorderPoint) throw new InventoryException("Need 0 <= reorderPoint < maxLevel");
    }
}
