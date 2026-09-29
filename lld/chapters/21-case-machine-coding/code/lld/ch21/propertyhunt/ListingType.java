package lld.ch21.propertyhunt;

public enum ListingType {
    SELL, RENT;

    public static ListingType parse(String s) {
        return switch (s.trim().toLowerCase()) {
            case "sell", "sale" -> SELL;
            case "rent" -> RENT;
            default -> throw new HuntException("Listing type must be sell or rent: " + s);
        };
    }
}
