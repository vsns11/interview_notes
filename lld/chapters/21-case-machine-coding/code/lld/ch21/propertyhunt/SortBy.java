package lld.ch21.propertyhunt;

import java.util.Comparator;

public enum SortBy {
    PRICE(Comparator.comparingLong(Property::price)),
    SIZE(Comparator.comparingDouble(Property::sizeSqft)),
    NONE(Comparator.comparingInt(Property::id));

    final Comparator<Property> comparator;
    SortBy(Comparator<Property> c) { this.comparator = c.thenComparingInt(Property::id); }

    public static SortBy parse(String s) { return s == null ? NONE : valueOf(s.trim().toUpperCase()); }
}
