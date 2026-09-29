package ch05.books;

import java.util.List;

/** A stable JSON shape for one page of results. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
