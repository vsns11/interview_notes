package ch05.books;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** What the API returns. Jackson 3 turns records into JSON objects. */
public record Book(
        Long id,
        String title,
        String author,
        String isbn,
        @JsonProperty("published_year") int year,   // rename one JSON field
        LocalDate addedOn) {
}
