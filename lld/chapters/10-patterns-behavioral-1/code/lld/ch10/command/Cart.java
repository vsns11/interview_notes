package lld.ch10.command;

import java.util.ArrayList;
import java.util.List;

/** The RECEIVER: knows how to change itself and nothing about commands. */
public final class Cart {
    public record Line(String sku, int qty) {}

    private final List<Line> lines = new ArrayList<>();
    private int discountPercent = 0;

    public void add(String sku, int qty) { lines.add(new Line(sku, qty)); }

    /** Removes the most recent line matching sku and qty. */
    public void remove(String sku, int qty) {
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (lines.get(i).equals(new Line(sku, qty))) {
                lines.remove(i);
                return;
            }
        }
    }

    public int discountPercent() { return discountPercent; }
    public void setDiscount(int percent) { this.discountPercent = percent; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cart[");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(lines.get(i).sku()).append(" x").append(lines.get(i).qty());
        }
        return sb.append("] discount=").append(discountPercent).append('%').toString();
    }
}
