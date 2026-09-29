package lld.ch21.inventory;

/** Observer: purchasing, email, dashboards... anyone who reacts to low stock. */
public interface ReorderListener {
    void lowStock(Product product, int totalOnHand, int suggestedOrderQty);
}
