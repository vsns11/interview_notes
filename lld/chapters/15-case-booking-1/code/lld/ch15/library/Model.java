package lld.ch15.library;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

enum BookFormat { HARDCOVER, PAPERBACK, AUDIO_BOOK, EBOOK, NEWSPAPER, MAGAZINE, JOURNAL }

enum BookStatus { AVAILABLE, RESERVED, LOANED, LOST }

enum AccountStatus { ACTIVE, CLOSED, CANCELED, BLACKLISTED }

record Author(String name) {}

/** Where a copy physically sits. */
record Rack(int number, String location) {}

/** The TITLE: one per ISBN, however many copies the library owns. Immutable. */
record Book(String isbn, String title, String subject, String publisher,
            LocalDate publicationDate, List<Author> authors) {
    Book { authors = List.copyOf(authors); }
}

/** One PHYSICAL COPY of a Book: own barcode, own rack, own status. Guarded by its own monitor. */
final class BookItem {
    private final String barcode;
    private final Book book;
    private final BookFormat format;
    private final Rack rack;
    private final BigDecimal price;          // money: BigDecimal, never double
    private BookStatus status = BookStatus.AVAILABLE;
    private String heldForMemberId;          // set only while status == RESERVED

    BookItem(String barcode, Book book, BookFormat format, Rack rack, BigDecimal price) {
        this.barcode = barcode; this.book = book; this.format = format;
        this.rack = rack; this.price = price;
    }
    String barcode()               { return barcode; }
    Book book()                    { return book; }
    Rack rack()                    { return rack; }
    BookFormat format()            { return format; }
    BigDecimal price()             { return price; }
    synchronized BookStatus status()      { return status; }
    synchronized String heldForMemberId() { return heldForMemberId; }

    // State changes are package-private: only LendingService calls them, while holding this monitor.
    void markLoaned()               { status = BookStatus.LOANED;    heldForMemberId = null; }
    void markAvailable()            { status = BookStatus.AVAILABLE; heldForMemberId = null; }
    void holdFor(String memberId)   { status = BookStatus.RESERVED;  heldForMemberId = memberId; }
    void markLost()                 { status = BookStatus.LOST;      heldForMemberId = null; }
}

/** Each account owns exactly one card (composition): the card's barcode identifies the person at the desk. */
record LibraryCard(String cardNumber, String barcode, LocalDate issuedOn) {}

abstract sealed class Account permits Member, Librarian {
    private final String id;
    private final String name;
    private final LibraryCard card;
    private volatile AccountStatus status = AccountStatus.ACTIVE;

    Account(String id, String name, LibraryCard card) { this.id = id; this.name = name; this.card = card; }
    String id()                { return id; }
    String name()              { return name; }
    LibraryCard card()         { return card; }
    AccountStatus status()     { return status; }
    void setStatus(AccountStatus s) { status = s; }
}

/** A member is a person with a quota. The count is guarded by the member's monitor. */
final class Member extends Account {
    private int booksCheckedOut;
    Member(String id, String name, LibraryCard card) { super(id, name, card); }
    int booksCheckedOut()   { return booksCheckedOut; }
    void incrementLoans()   { booksCheckedOut++; }
    void decrementLoans()   { booksCheckedOut--; }
}

final class Librarian extends Account {
    Librarian(String id, String name, LibraryCard card) { super(id, name, card); }
}

/** One checkout of one copy by one member. Answers "who has this copy?" and "what does this member have?". */
final class BookLending {
    private final String barcode;
    private final String memberId;
    private final LocalDate creationDate;
    private volatile LocalDate dueDate;      // volatile: read by the overdue job without the item lock
    private volatile LocalDate returnDate;

    BookLending(String barcode, String memberId, LocalDate creationDate, LocalDate dueDate) {
        this.barcode = barcode; this.memberId = memberId;
        this.creationDate = creationDate; this.dueDate = dueDate;
    }
    String barcode()        { return barcode; }
    String memberId()       { return memberId; }
    LocalDate creationDate(){ return creationDate; }
    LocalDate dueDate()     { return dueDate; }
    LocalDate returnDate()  { return returnDate; }
    void renewUntil(LocalDate d) { dueDate = d; }
    void close(LocalDate d)      { returnDate = d; }
}

record Fine(String memberId, String barcode, BigDecimal amount, LocalDate date) {}

final class LibraryException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    LibraryException(String msg) { super(msg); }
}
