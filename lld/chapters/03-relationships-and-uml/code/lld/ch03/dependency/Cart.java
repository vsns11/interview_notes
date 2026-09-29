package lld.ch03.dependency;

import java.util.List;

public record Cart(List<Money> itemPrices) {
    public Money subtotal() {
        return itemPrices.stream().reduce(Money.of("0"), Money::plus);
    }
}
