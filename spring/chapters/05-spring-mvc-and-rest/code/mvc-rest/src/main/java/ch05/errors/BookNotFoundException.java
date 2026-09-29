package ch05.errors;

public class BookNotFoundException extends RuntimeException {
    private final long bookId;

    public BookNotFoundException(long bookId) {
        super("Book " + bookId + " does not exist");
        this.bookId = bookId;
    }

    public long bookId() { return bookId; }
}
