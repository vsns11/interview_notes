package lld.ch03.library;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LendingPolicy policy = new StandardPolicy();
        Library lib = new Library("Town Library");
        Librarian lee = new Librarian("Lee", policy, lib);

        Book dune = new Book("978-0441013593", "Dune", "Frank Herbert");
        Book hobbit = new Book("978-0547928227", "The Hobbit", "J.R.R. Tolkien");
        lee.addCopy("B-1", dune);
        lee.addCopy("B-2", dune);
        lee.addCopy("B-3", hobbit);
        lee.addCopy("B-4", hobbit);

        Member maya = new Member("Maya", policy);
        LocalDate day1 = LocalDate.of(2026, 3, 1);

        // Sequence: member -> catalog.search -> (self) lookUpTitleIndex -> copies; member -> copy.checkout -> due
        List<BookItem> hits = lib.catalog().search("dune");
        System.out.println("search(\"dune\") -> " + hits);
        LocalDate due = maya.checkout(hits.get(0), day1);
        System.out.println("Maya borrowed B-1, due " + due);
        maya.checkout(lib.catalog().search("the hobbit").get(0), day1);
        System.out.println("Maya on loan: " + maya.onLoan());

        try {
            maya.checkout(hits.get(1), day1);   // <<include>> limit check fires
        } catch (IllegalStateException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        try {
            new Member("Omar", policy).checkout(hits.get(0), day1);   // copy already LOANED
        } catch (IllegalStateException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        BigDecimal onTime = maya.returnBook(hits.get(0), day1.plusDays(10));
        System.out.println("Returned B-1 on time, fine = " + onTime);
        BookItem hobbitCopy = maya.onLoan().get(0);
        BigDecimal late = maya.returnBook(hobbitCopy, day1.plusDays(17));   // <<extend>> fine applies
        System.out.println("Returned " + hobbitCopy.barcode() + " 3 days late, fine = " + late
                + ", Maya owes " + maya.finesDue());
        System.out.println("Librarian limit: " + policy.maxBooks(lee) + ", member limit: " + policy.maxBooks(maya));
    }
}
