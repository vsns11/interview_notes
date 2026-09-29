package lld.ch02.generics;

public record Product(Integer id, String title, long priceCents)
        implements Identifiable<Integer>, Comparable<Product> {
    @Override public int compareTo(Product o) { return Long.compare(priceCents, o.priceCents); }
}
