package lld.ch07.worked.step5;

/** The enum now just names a service and points at its Tariff (Strategy). */
public enum ServiceType {
    STANDARD("STD", new StandardTariff()),
    EXPRESS("EXP", new ExpressTariff()),
    FRAGILE("FRG", new FragileTariff());

    private final String code;
    private final Tariff tariff;

    ServiceType(String code, Tariff tariff) { this.code = code; this.tariff = tariff; }

    public String code() { return code; }
    public Tariff tariff() { return tariff; }

    public static ServiceType fromCode(String code) {
        for (ServiceType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("unknown type: " + code);
    }
}
