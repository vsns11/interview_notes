package ch13.books;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Functional routes are plain objects, so they can be tested with no Spring context at all. */
class FunctionalRoutesTest {

    BookRepository repo = mock(BookRepository.class);
    WebTestClient client = WebTestClient
            .bindToRouterFunction(new BookRoutes().bookRouter(new BookRoutes.BookHandler(repo)))
            .build();

    @Test
    void byIsbnFoundAndNotFound() {
        given(repo.findByIsbn("111")).willReturn(Mono.just(new Book(1L, "T", "A", "111")));
        given(repo.findByIsbn("222")).willReturn(Mono.empty());

        client.get().uri("/fn/books/111").exchange()
              .expectStatus().isOk()
              .expectHeader().valueEquals("X-Router", "functional")
              .expectBody().jsonPath("$.title").isEqualTo("T");
        client.get().uri("/fn/books/222").exchange().expectStatus().isNotFound();
        System.out.println("GET /fn/books/111 -> 200 (X-Router: functional), /fn/books/222 -> 404");
    }

    @Test
    void byAuthorNeedsTheParameter() {
        given(repo.findByAuthor("Evans")).willReturn(Flux.just(new Book(1L, "DDD", "Evans", "1")));

        client.get().uri("/fn/books?author=Evans").exchange()
              .expectStatus().isOk().expectBodyList(Book.class).hasSize(1);
        client.get().uri("/fn/books").exchange()
              .expectStatus().isBadRequest().expectBody(String.class).isEqualTo("author is required");
        System.out.println("GET /fn/books?author=Evans -> 200 [1 book], /fn/books -> 400 author is required");
    }
}
