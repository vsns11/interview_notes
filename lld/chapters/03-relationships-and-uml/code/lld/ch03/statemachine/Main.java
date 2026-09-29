package lld.ch03.statemachine;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Order happy = new Order("ORD-1", new BigDecimal("499.00"));
        happy.handle(new OrderEvent.Pay(new BigDecimal("499.00")));
        happy.handle(new OrderEvent.Ship("TRK-77"));
        happy.handle(new OrderEvent.Deliver());
        System.out.println("ORD-1 path:");
        happy.log().forEach(l -> System.out.println("  " + l));
        System.out.println("  final? " + happy.state().isFinal());

        Order refund = new Order("ORD-2", new BigDecimal("120.00"));
        tryEvent(refund, new OrderEvent.Pay(new BigDecimal("100.00")));   // guard fails
        tryEvent(refund, new OrderEvent.Pay(new BigDecimal("120.00")));
        tryEvent(refund, new OrderEvent.Cancel("changed mind"));
        tryEvent(refund, new OrderEvent.Ship("TRK-99"));                         // no transition from CANCELLED
        System.out.println("ORD-2 path:");
        refund.log().forEach(l -> System.out.println("  " + l));
    }

    private static void tryEvent(Order o, OrderEvent e) {
        try {
            o.handle(e);
        } catch (IllegalStateException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }
}
