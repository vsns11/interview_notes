package ch05.statics;

public class Ticket {
    public static final int MAX_PER_ORDER = 10;      // constant: static + final, UPPER_SNAKE_CASE
    private static int issued;                       // ONE copy shared by all Ticket objects
    private static final String PREFIX;

    static {                                         // static initializer: runs once, when the class is initialised
        PREFIX = "TKT-" + (2000 + 26);
        System.out.println("[Ticket class initialised]");
    }

    private final int number;                        // one copy PER object

    public Ticket() {
        issued++;
        number = issued;
    }

    public String id() { return PREFIX + "-" + number; }   // instance method: may use static AND instance members

    public static int issuedSoFar() { return issued; }      // static method: no 'this', only static members

    public static boolean canBuy(int quantity) {            // pure utility: all input comes from parameters
        return quantity > 0 && quantity <= MAX_PER_ORDER;
    }
}
