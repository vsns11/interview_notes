package ch06.objectmethods;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EqualsHashCode {

    static class NoEquals {                                    // inherits Object's identity-based equals/hashCode
        final String isbn;
        NoEquals(String isbn) { this.isbn = isbn; }
    }

    static class EqualsOnly {                                  // BROKEN: equals without hashCode
        final String isbn;
        EqualsOnly(String isbn) { this.isbn = isbn; }
        @Override public boolean equals(Object o) {
            return o instanceof EqualsOnly other && isbn.equals(other.isbn);
        }
    }

    static final class Book {                                  // correct
        private final String isbn;
        private final String title;
        Book(String isbn, String title) { this.isbn = isbn; this.title = title; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;                         // 1. same reference
            if (!(o instanceof Book other)) return false;       // 2. right type (also handles null)
            return isbn.equals(other.isbn)                      // 3. compare significant fields
                    && Objects.equals(title, other.title);     //    (null-safe for nullable fields)
        }
        @Override public int hashCode() { return Objects.hash(isbn, title); }   // same fields as equals
        @Override public String toString() { return "Book[isbn=" + isbn + ", title=" + title + "]"; }
    }

    public static void main(String[] args) {
        NoEquals a1 = new NoEquals("123"), a2 = new NoEquals("123");
        System.out.println("NoEquals:   equals=" + a1.equals(a2) + ", toString=" + a1.toString().replaceAll("@.*", "@<hash>"));

        EqualsOnly b1 = new EqualsOnly("123"), b2 = new EqualsOnly("123");
        Set<EqualsOnly> broken = new HashSet<>();
        broken.add(b1);
        System.out.println("EqualsOnly: equals=" + b1.equals(b2) + ", set.contains(equal copy)=" + broken.contains(b2)
                + "  <- hashCode not overridden");

        Book c1 = new Book("123", "Dune"), c2 = new Book("123", "Dune");
        Set<Book> books = new HashSet<>();
        books.add(c1);
        System.out.println("Book:       equals=" + c1.equals(c2) + ", same hash=" + (c1.hashCode() == c2.hashCode())
                + ", set.contains(equal copy)=" + books.contains(c2) + ", == " + (c1 == c2));
        System.out.println("toString:   " + c1);
        System.out.println("equals(null)=" + c1.equals(null) + ", Objects.equals(null, null)=" + Objects.equals(null, null));
    }
}
