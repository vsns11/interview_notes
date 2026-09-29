package lld.ch03.library;

import java.time.LocalDate;
import java.util.Optional;

/** One physical copy. Association BookItem * --> 1 Book, and 0..1 borrower. */
public final class BookItem {
    static final int LOAN_DAYS = 14;

    private final String barcode;
    private final Book book;
    private BookStatus status = BookStatus.AVAILABLE;
    private Member borrower;      // 0..1
    private LocalDate dueDate;    // set only while LOANED

    public BookItem(String barcode, Book book) {
        this.barcode = barcode;
        this.book = book;
    }

    /** Called by Member. Returns the due date: the one return the sequence diagram draws. */
    LocalDate checkout(Member m, LocalDate today) {
        if (status != BookStatus.AVAILABLE) {
            throw new IllegalStateException(barcode + " is " + status);
        }
        status = BookStatus.LOANED;
        borrower = m;
        dueDate = today.plusDays(LOAN_DAYS);
        return dueDate;
    }

    LocalDate checkIn() {
        if (status != BookStatus.LOANED) throw new IllegalStateException(barcode + " is not on loan");
        LocalDate due = dueDate;
        status = BookStatus.AVAILABLE;
        borrower = null;
        dueDate = null;
        return due;
    }

    public String barcode() { return barcode; }
    public Book book() { return book; }
    public BookStatus status() { return status; }
    public Optional<Member> borrower() { return Optional.ofNullable(borrower); }

    @Override public String toString() { return barcode + "[" + book.title() + ", " + status + "]"; }
}
