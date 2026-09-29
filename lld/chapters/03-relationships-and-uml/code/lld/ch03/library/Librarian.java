package lld.ch03.library;

/** Generalization between actors: a librarian can do everything a member can, plus more. */
public final class Librarian extends Member {
    private final Library library;

    public Librarian(String name, LendingPolicy policy, Library library) {
        super(name, policy);
        this.library = library;
    }

    public BookItem addCopy(String barcode, Book book) { return library.addCopy(barcode, book); }
}
