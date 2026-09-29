package ch05.statics;

import static java.lang.Math.max;       // static import: use max(...) without 'Math.'
import static java.lang.Math.PI;

public class StaticDemo {
    public static void main(String[] args) {
        System.out.println("before first use");
        System.out.println("MAX_PER_ORDER = " + Ticket.MAX_PER_ORDER);  // compile-time constant: inlined, no class init
        System.out.println("issued = " + Ticket.issuedSoFar());           // first real use triggers initialisation
        Ticket t1 = new Ticket(), t2 = new Ticket(), t3 = new Ticket();
        System.out.println(t1.id() + ", " + t2.id() + ", " + t3.id());
        System.out.println("issued = " + Ticket.issuedSoFar() + ", canBuy(12) = " + Ticket.canBuy(12));
        System.out.println("max(3, 8) = " + max(3, 8) + ", PI = " + PI);
    }
}
