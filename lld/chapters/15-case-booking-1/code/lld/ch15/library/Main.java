package lld.ch15.library;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        SimClock clock = new SimClock(Instant.parse("2026-03-01T10:00:00Z"));
        ConsoleNotifier notifier = new ConsoleNotifier();
        Library lib = new Library("City Library", "1 Main Street", clock,
                new PerDayFine(new BigDecimal("0.50")), notifier);
        LendingService desk = lib.lending();
        LocalDate today = LocalDate.of(2026, 3, 1);

        Librarian lucy = new Librarian("L1", "Lucy", new LibraryCard("C-L1", "BC-L1", today));
        Book dune = new Book("978-0441013593", "Dune", "Science Fiction", "Ace",
                LocalDate.of(1965, 8, 1), List.of(new Author("Frank Herbert")));
        Book hobbit = new Book("978-0547928227", "The Hobbit", "Fantasy", "Mariner",
                LocalDate.of(1937, 9, 21), List.of(new Author("J.R.R. Tolkien")));
        Book lotr = new Book("978-0544003415", "The Lord of the Rings", "Fantasy", "Mariner",
                LocalDate.of(1954, 7, 29), List.of(new Author("J.R.R. Tolkien")));
        Rack r1 = new Rack(1, "Aisle A");
        lib.addBookItem(lucy, new BookItem("B-DUNE-1", dune, BookFormat.PAPERBACK, r1, new BigDecimal("9.99")));
        for (int i = 1; i <= 6; i++)
            lib.addBookItem(lucy, new BookItem("B-HOB-" + i, hobbit, BookFormat.HARDCOVER, r1, new BigDecimal("14.99")));
        lib.addBookItem(lucy, new BookItem("B-LOTR-1", lotr, BookFormat.HARDCOVER, r1, new BigDecimal("29.99")));

        for (int i = 1; i <= 9; i++)
            lib.registerMember(lucy, new Member("M" + i, "Member " + i, new LibraryCard("C-M" + i, "BC-M" + i, today)));

        System.out.println("== 1. Search the catalog ==");
        System.out.println("  author 'j.r.r. tolkien' -> " + titles(lib.catalog().searchByAuthor("j.r.r. tolkien")));
        System.out.println("  subject 'Fantasy'       -> " + titles(lib.catalog().searchBySubject("Fantasy")));
        System.out.println("  published 1965-08-01    -> " + titles(lib.catalog().searchByPublicationDate(LocalDate.of(1965, 8, 1))));
        System.out.println("  free copies of Hobbit   -> " + lib.catalog().availableCopies(hobbit.isbn()));

        System.out.println("\n== 2. Checkout limit (max 5 books per member) ==");
        for (int i = 1; i <= 6; i++) {
            try {
                BookLending l = desk.checkout("M1", "B-HOB-" + i);
                System.out.println("  M1 took " + l.barcode() + ", due " + l.dueDate());
            } catch (LibraryException e) {
                System.out.println("  REFUSED: " + e.getMessage());
            }
        }
        System.out.println("  M1 has " + desk.loansOf("M1"));

        System.out.println("\n== 3. 8 members race for the ONLY copy of Dune ==");
        AtomicInteger winners = new AtomicInteger(), losers = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(8)) {
            for (int i = 2; i <= 9; i++) {
                String id = "M" + i;
                pool.submit(() -> {
                    start.await();
                    try { desk.checkout(id, "B-DUNE-1"); winners.incrementAndGet(); }
                    catch (LibraryException e) { losers.incrementAndGet(); }
                    return null;
                });
            }
            start.countDown();                    // release all threads at once
        }                                         // close() waits for every task
        String holder = desk.whoHas("B-DUNE-1");
        System.out.println("  winners=" + winners + " losers=" + losers + " (exactly one copy was lent)");
        System.out.println("  holder = " + holder + "  (which member wins varies from run to run)");

        System.out.println("\n== 4. Reserve a title with no free copy (FIFO waitlist) ==");
        String first = holder.equals("M2") ? "M3" : "M2";
        String second = holder.equals("M4") ? "M5" : "M4";
        System.out.println("  " + first + " position " + desk.reserve(first, dune.isbn()));
        System.out.println("  " + second + " position " + desk.reserve(second, dune.isbn()));
        try { desk.reserve("M6", hobbit.isbn()); }
        catch (LibraryException e) { System.out.println("  M6 reserve Hobbit REFUSED: " + e.getMessage()); }

        System.out.println("\n== 5. Renew is blocked while someone waits ==");
        try { desk.renew(holder, "B-DUNE-1"); }
        catch (LibraryException e) { System.out.println("  REFUSED: " + e.getMessage()); }

        System.out.println("\n== 6. 13 days later: overdue notices, late return, fine, hand-over ==");
        clock.advance(Duration.ofDays(13));
        desk.sendOverdueNotices();
        BigDecimal fine = desk.returnBook("B-DUNE-1");
        System.out.println("  fine charged = " + Money.fmt(fine) + " (3 days late x $0.50)");
        try { desk.checkout(second, "B-DUNE-1"); }
        catch (LibraryException e) { System.out.println("  " + second + " REFUSED: " + e.getMessage()); }
        System.out.println("  " + first + " checks out held copy, due " + desk.checkout(first, "B-DUNE-1").dueDate());
        System.out.println("  who has B-DUNE-1? " + desk.whoHas("B-DUNE-1"));

        System.out.println("\n== 7. Renew on time with nobody waiting ==");
        desk.returnBook("B-HOB-1");                     // 13 days: late, fine charged
        desk.checkout("M6", "B-HOB-1");
        clock.advance(Duration.ofDays(4));
        System.out.println("  M6 renewed B-HOB-1, new due " + desk.renew("M6", "B-HOB-1").dueDate());
        System.out.println("  fines on record: " + desk.fines().size());

        System.out.println("\n== 8. Librarian housekeeping: remove a copy, cancel a membership ==");
        try { lib.removeBookItem(lucy, "B-DUNE-1"); }
        catch (LibraryException e) { System.out.println("  REFUSED: " + e.getMessage()); }
        lib.removeBookItem(lucy, "B-LOTR-1");
        System.out.println("  copies of LOTR left: " + lib.catalog().copiesOf(lotr.isbn()).size());
        try { lib.cancelMembership(lucy, "M6"); }
        catch (LibraryException e) { System.out.println("  REFUSED: " + e.getMessage()); }
        lib.cancelMembership(lucy, "M8");
        try { desk.checkout("M8", "B-HOB-6"); }
        catch (LibraryException e) { System.out.println("  REFUSED: " + e.getMessage()); }
    }

    private static List<String> titles(List<Book> books) { return books.stream().map(Book::title).toList(); }
}
