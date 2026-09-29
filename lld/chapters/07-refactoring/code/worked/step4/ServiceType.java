package lld.ch07.worked.step4;

/** Replace Primitive with Object (type code String -> enum). Invalid codes fail at the boundary. */
public enum ServiceType {
    STANDARD("STD"), EXPRESS("EXP"), FRAGILE("FRG");

    private final String code;

    ServiceType(String code) { this.code = code; }

    public String code() { return code; }

    public static ServiceType fromCode(String code) {
        for (ServiceType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("unknown type: " + code);
    }
}
