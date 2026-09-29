package lld.ch15.library;

import java.time.Clock;

/** Facade: the one object the desk software talks to. Owns the catalog and the lending rules. */
final class Library {
    private final String name;
    private final String address;
    private final Catalog catalog = new Catalog();
    private final LendingService lending;

    Library(String name, String address, Clock clock, FinePolicy finePolicy, Notifier notifier) {
        this.name = name; this.address = address;
        this.lending = new LendingService(catalog, clock, finePolicy, notifier);
    }

    /** Only a librarian may add stock (the use-case diagram's "Add book item"). */
    void addBookItem(Account by, BookItem item) {
        if (!(by instanceof Librarian)) throw new LibraryException(by.id() + " is not a librarian");
        catalog.addItem(item);
    }

    void removeBookItem(Account by, String barcode) {
        if (!(by instanceof Librarian)) throw new LibraryException(by.id() + " is not a librarian");
        catalog.removeItem(barcode);
    }

    void registerMember(Account by, Member m) {
        if (!(by instanceof Librarian)) throw new LibraryException(by.id() + " is not a librarian");
        lending.register(m);
    }

    void cancelMembership(Account by, String memberId) {
        if (!(by instanceof Librarian)) throw new LibraryException(by.id() + " is not a librarian");
        lending.cancelMembership(memberId);
    }

    String name()            { return name; }
    String address()         { return address; }
    Catalog catalog()        { return catalog; }
    LendingService lending() { return lending; }
}
