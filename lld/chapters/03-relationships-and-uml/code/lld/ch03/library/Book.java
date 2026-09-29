package lld.ch03.library;

/** The title (one per ISBN). Many physical copies (BookItem) point at one Book. */
public record Book(String isbn, String title, String author) { }
