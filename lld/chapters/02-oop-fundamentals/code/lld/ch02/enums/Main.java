package lld.ch02.enums;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        List<Coin> inserted = List.of(Coin.QUARTER, Coin.QUARTER, Coin.DIME, Coin.PENNY);
        int total = inserted.stream().mapToInt(Coin::cents).sum();
        System.out.println("inserted " + inserted + " = " + total + " cents");

        Map<Coin, Integer> counts = new EnumMap<>(Coin.class);   // compact, ordered by declaration
        for (Coin c : inserted) counts.merge(c, 1, Integer::sum);
        System.out.println("counts: " + counts);

        System.out.println("valueOf(\"DIME\") = " + Coin.valueOf("DIME") + ", ordinal " + Coin.DIME.ordinal());

        OrderStatus s = OrderStatus.PLACED;
        for (OrderStatus target : List.of(OrderStatus.SHIPPED, OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED)) {
            if (s.canMoveTo(target)) { s = target; System.out.println("moved to " + s); }
            else System.out.println("refused " + s + " -> " + target);
        }
        System.out.println(s + " is final? " + s.isFinal());

        String label = switch (s) {                           // exhaustive switch: no default needed
            case PLACED, PAID -> "in progress";
            case SHIPPED -> "on the way";
            case DELIVERED -> "done";
            case CANCELLED -> "stopped";
        };
        System.out.println("label: " + label);
    }
}
