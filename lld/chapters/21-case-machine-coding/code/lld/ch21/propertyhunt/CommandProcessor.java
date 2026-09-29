package lld.ch21.propertyhunt;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses '|'-separated commands. Inside a field, ',' separates multiple values (bonus requirement).
 * "null" or an empty field means "not given".
 */
public class CommandProcessor {
    private final PropertyService svc;
    public CommandProcessor(PropertyService svc) { this.svc = svc; }

    public void execute(String line) {
        System.out.println("-> " + line);
        String[] f = Arrays.stream(line.split("\\|", -1)).map(String::trim).toArray(String[]::new);
        try {
            switch (f[0].toLowerCase()) {
                case "registeruser" -> { svc.registerUser(f[1]); out("User registered successfully."); }
                case "listproperty" -> {
                    Set<String> nearby = f.length > 8 && given(f[8]) ? csv(f[8]) : Set.of();
                    Property p = svc.list(f[1], f[2], f[3], nearby, Price.parse(f[4]), ListingType.parse(f[5]),
                            AreaUnit.parseSize(f[6], AreaUnit.SQFT), rooms(f[7]));
                    out("Property listed successfully. id=" + p.id());
                }
                case "search" -> printTable(svc.search(f[1], criteria(f)), false);
                case "shortlist" -> { svc.shortlist(f[1], Integer.parseInt(f[2])); out("Shortlisted property"); }
                case "viewshortlisted" -> printTable(svc.shortlisted(f[1]), true);
                case "viewlisted" -> printTable(svc.listedBy(f[1]), true);
                case "viewsold" -> printTable(svc.soldBy(f[1]), true);
                case "marksold" -> { svc.markSold(f[1], Integer.parseInt(f[2])); out("Property marked SOLD"); }
                default -> out("ERROR: unknown command " + f[0]);
            }
        } catch (HuntException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            out("ERROR: " + e.getMessage());
        }
    }

    /** Search|user|locations|priceRange|type|sizeRange|rooms|sortBy */
    private SearchCriteria criteria(String[] f) {
        SearchCriteria.Builder b = SearchCriteria.builder();
        if (given(f[2])) b.locations(csv(f[2]));
        if (given(f[3])) {
            String[] r = f[3].split("-");                       // "X-Y" inclusive, "X" = at most X
            if (r.length == 2) b.price(Price.parse(r[0]), Price.parse(r[1]));
            else b.price(null, Price.parse(r[0]));
        }
        if (given(f[4])) b.type(ListingType.parse(f[4]));
        if (given(f[5])) {
            String[] r = f[5].split("-");                       // "X-Y" inclusive, "X" = at least X
            if (r.length == 2) {
                AreaUnit unit = unitOf(r[1]);                   // "1000-2000 sqft": unit written once
                b.size(AreaUnit.parseSize(r[0], unit), AreaUnit.parseSize(r[1], unit));
            } else b.size(AreaUnit.parseSize(r[0], AreaUnit.SQFT), null);
        }
        if (given(f[6])) b.rooms(csv(f[6]).stream().map(CommandProcessor::rooms).collect(Collectors.toSet()));
        if (f.length > 7 && given(f[7])) b.sortBy(SortBy.parse(f[7]));
        return b.build();
    }

    private static AreaUnit unitOf(String s) {
        String unit = s.replaceAll("[0-9.\\s]", "");
        return unit.isEmpty() ? AreaUnit.SQFT : AreaUnit.parse(unit);
    }

    private static int rooms(String s) { return Integer.parseInt(s.toUpperCase().replace("BHK", "").trim()); }
    private static boolean given(String s) { return !s.isEmpty() && !s.equalsIgnoreCase("null"); }
    private static Set<String> csv(String s) {
        return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.toSet());
    }

    private static void printTable(List<Property> ps, boolean withStatus) {
        if (ps.isEmpty()) { out("<- (no properties)"); return; }
        out("<- %-3s %-20s %-10s %-6s %-9s %-5s %-5s%s".formatted("Id", "Title", "Location", "Price", "Size",
                "Rooms", "For", withStatus ? " Status" : ""));
        for (Property p : ps)
            out("<- %-3d %-20s %-10s %-6s %-9s %-5s %-5s%s".formatted(p.id(), p.title(), p.location(),
                    Price.format(p.price()), Math.round(p.sizeSqft()) + "sqft", p.rooms() + "BHK",
                    p.type() == ListingType.SELL ? "Sale" : "Rent", withStatus ? " " + p.status() : ""));
    }

    private static void out(String s) { System.out.println(s); }
}
