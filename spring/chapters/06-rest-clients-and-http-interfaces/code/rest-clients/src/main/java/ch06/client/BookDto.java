package ch06.client;

/** Our own view of the remote book. Unknown JSON fields are ignored (Jackson 3 default). */
public record BookDto(Long id, String title, String author) {}
