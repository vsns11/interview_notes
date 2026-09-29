package lld.ch21.propertyhunt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class PropertyService {
    private final Map<String, Set<Integer>> shortlists = new ConcurrentHashMap<>();  // user -> ids (the user registry)
    private final Map<Integer, Property> properties = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    public void registerUser(String name) {
        if (shortlists.putIfAbsent(name, ConcurrentHashMap.newKeySet()) != null)
            throw new HuntException("User already registered: " + name);
    }

    public Property list(String owner, String title, String location, Set<String> nearby, long price,
                         ListingType type, double sizeSqft, int rooms) {
        requireUser(owner);
        if (price <= 0 || sizeSqft <= 0 || rooms <= 0) throw new HuntException("Price, size and rooms must be positive");
        Property p = new Property(seq.incrementAndGet(), owner, title, location.trim().toLowerCase(),
                lower(nearby), price, type, sizeSqft, rooms);
        properties.put(p.id(), p);
        return p;
    }

    /** Only AVAILABLE properties. Exact location matches first, nearby matches last, then the chosen sort. */
    public List<Property> search(String user, SearchCriteria c) {
        requireUser(user);
        Set<String> locs = c.locations() == null ? null : lower(c.locations());
        List<Predicate<Property>> filters = new ArrayList<>();
        filters.add(p -> p.status() == PropertyStatus.AVAILABLE);
        if (locs != null) filters.add(p -> locationRank(p, locs) < 2);
        if (c.minPrice() != null) filters.add(p -> p.price() >= c.minPrice());
        if (c.maxPrice() != null) filters.add(p -> p.price() <= c.maxPrice());
        if (c.type() != null) filters.add(p -> p.type() == c.type());
        if (c.minSize() != null) filters.add(p -> p.sizeSqft() >= c.minSize());
        if (c.maxSize() != null) filters.add(p -> p.sizeSqft() <= c.maxSize());
        if (c.rooms() != null) filters.add(p -> c.rooms().contains(p.rooms()));
        Predicate<Property> all = filters.stream().reduce(x -> true, Predicate::and);

        Comparator<Property> order = locs == null ? c.sortBy().comparator
                : Comparator.<Property>comparingInt(p -> locationRank(p, locs)).thenComparing(c.sortBy().comparator);
        return properties.values().stream().filter(all).sorted(order).toList();
    }

    public void shortlist(String user, int propertyId) {
        requireUser(user);
        Property p = get(propertyId);
        if (p.ownerId().equals(user)) throw new HuntException("You cannot shortlist your own property");
        if (p.status() == PropertyStatus.SOLD) throw new HuntException("Property " + propertyId + " is already sold");
        if (!shortlists.get(user).add(propertyId)) throw new HuntException("Property " + propertyId + " already shortlisted");
    }

    /** Includes sold ones; the caller prints status so the buyer sees "SOLD". */
    public List<Property> shortlisted(String user) {
        requireUser(user);
        return shortlists.get(user).stream().map(properties::get).sorted(Comparator.comparingInt(Property::id)).toList();
    }

    public List<Property> listedBy(String user) {
        requireUser(user);
        return properties.values().stream().filter(p -> p.ownerId().equals(user))
                .sorted(Comparator.comparingInt(Property::id)).toList();
    }

    public List<Property> soldBy(String user) {
        return listedBy(user).stream().filter(p -> p.status() == PropertyStatus.SOLD).toList();
    }

    public void markSold(String user, int propertyId) {
        requireUser(user);
        Property p = get(propertyId);
        if (!p.ownerId().equals(user)) throw new HuntException("Only the owner can mark property " + propertyId + " sold");
        p.markSold();
    }

    /** 0 = exact location match, 1 = nearby match, 2 = no match. */
    private static int locationRank(Property p, Set<String> locs) {
        if (locs.contains(p.location())) return 0;
        return p.nearby().stream().anyMatch(locs::contains) ? 1 : 2;
    }

    private Property get(int id) {
        Property p = properties.get(id);
        if (p == null) throw new HuntException("Property does not exist: " + id);
        return p;
    }

    private void requireUser(String u) { if (!shortlists.containsKey(u)) throw new HuntException("User not registered: " + u); }

    private static Set<String> lower(Set<String> s) {
        Set<String> out = new LinkedHashSet<>();
        s.forEach(x -> out.add(x.trim().toLowerCase()));
        return out;
    }
}
