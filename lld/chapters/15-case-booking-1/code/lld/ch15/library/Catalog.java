package lld.ch15.library;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** The four searches the requirements ask for. */
interface Search {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchBySubject(String subject);
    List<Book> searchByPublicationDate(LocalDate date);
}

/**
 * Keeps one index per search so every lookup is a map read, not a scan.
 * It also knows every copy of every title, which answers "how many copies are free?".
 * Reads vastly outnumber writes in a library, so concurrent maps + copy-on-write lists fit well.
 */
final class Catalog implements Search {
    private final Map<String, List<Book>> byTitle   = new ConcurrentHashMap<>();
    private final Map<String, List<Book>> byAuthor  = new ConcurrentHashMap<>();
    private final Map<String, List<Book>> bySubject = new ConcurrentHashMap<>();
    private final Map<LocalDate, List<Book>> byDate = new ConcurrentHashMap<>();
    private final Map<String, Book> books = new ConcurrentHashMap<>();                  // isbn -> title
    private final Map<String, List<BookItem>> copies = new ConcurrentHashMap<>();        // isbn -> copies
    private final Map<String, BookItem> itemsByBarcode = new ConcurrentHashMap<>();

    void addBook(Book b) {
        if (books.putIfAbsent(b.isbn(), b) != null) return;       // already indexed
        index(byTitle, key(b.title()), b);
        b.authors().forEach(a -> index(byAuthor, key(a.name()), b));
        index(bySubject, key(b.subject()), b);
        byDate.computeIfAbsent(b.publicationDate(), d -> new CopyOnWriteArrayList<>()).add(b);
    }

    void addItem(BookItem item) {
        addBook(item.book());
        if (itemsByBarcode.putIfAbsent(item.barcode(), item) != null)
            throw new LibraryException("duplicate barcode " + item.barcode());
        copies.computeIfAbsent(item.book().isbn(), k -> new CopyOnWriteArrayList<>()).add(item);
    }

    /** Take a copy out of stock (weeded, damaged beyond repair). A copy that is out on loan or held cannot go. */
    void removeItem(String barcode) {
        BookItem item = item(barcode);
        synchronized (item) {
            if (item.status() == BookStatus.LOANED || item.status() == BookStatus.RESERVED)
                throw new LibraryException(barcode + " is " + item.status() + ", cannot remove it now");
            itemsByBarcode.remove(barcode);
            copies.getOrDefault(item.book().isbn(), List.of()).remove(item);
        }
    }

    BookItem item(String barcode) {
        BookItem i = itemsByBarcode.get(barcode);
        if (i == null) throw new LibraryException("unknown barcode " + barcode);
        return i;
    }

    List<BookItem> copiesOf(String isbn) { return copies.getOrDefault(isbn, List.of()); }

    long availableCopies(String isbn) {
        return copiesOf(isbn).stream().filter(c -> c.status() == BookStatus.AVAILABLE).count();
    }

    @Override public List<Book> searchByTitle(String t)     { return find(byTitle, key(t)); }
    @Override public List<Book> searchByAuthor(String a)    { return find(byAuthor, key(a)); }
    @Override public List<Book> searchBySubject(String s)   { return find(bySubject, key(s)); }
    @Override public List<Book> searchByPublicationDate(LocalDate d) { return List.copyOf(byDate.getOrDefault(d, List.of())); }

    private static String key(String s) { return s.trim().toLowerCase(Locale.ROOT); }

    private static void index(Map<String, List<Book>> idx, String k, Book b) {
        idx.computeIfAbsent(k, x -> new CopyOnWriteArrayList<>()).add(b);
    }

    private static List<Book> find(Map<String, List<Book>> idx, String k) {
        return idx.getOrDefault(k, List.of()).stream().sorted(Comparator.comparing(Book::title)).toList();
    }
}
