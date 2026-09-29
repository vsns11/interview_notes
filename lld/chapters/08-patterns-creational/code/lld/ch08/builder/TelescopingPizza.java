package lld.ch08.builder;

/** The PROBLEM: telescoping constructors. Each new optional field adds another overload. */
public final class TelescopingPizza {
    private final String size;          // required
    private final String crust;         // optional
    private final boolean cheese;       // optional
    private final boolean olives;       // optional
    private final boolean jalapenos;    // optional

    public TelescopingPizza(String size) { this(size, "regular"); }
    public TelescopingPizza(String size, String crust) { this(size, crust, true); }
    public TelescopingPizza(String size, String crust, boolean cheese) { this(size, crust, cheese, false); }
    public TelescopingPizza(String size, String crust, boolean cheese, boolean olives) {
        this(size, crust, cheese, olives, false);
    }
    public TelescopingPizza(String size, String crust, boolean cheese, boolean olives, boolean jalapenos) {
        this.size = size; this.crust = crust; this.cheese = cheese; this.olives = olives; this.jalapenos = jalapenos;
    }

    @Override public String toString() {
        return "Pizza[size=" + size + ", crust=" + crust + ", cheese=" + cheese
                + ", olives=" + olives + ", jalapenos=" + jalapenos + "]";
    }
}
