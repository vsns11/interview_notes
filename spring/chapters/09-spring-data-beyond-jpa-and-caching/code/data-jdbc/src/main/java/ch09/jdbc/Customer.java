package ch09.jdbc;

import org.springframework.data.annotation.Id;

/** Its own aggregate. An immutable record works: Spring Data creates a copy with the generated id. */
public record Customer(@Id Long id, String name, String email) {}
