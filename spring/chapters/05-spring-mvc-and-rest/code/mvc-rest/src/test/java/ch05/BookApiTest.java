package ch05;

import static org.assertj.core.api.Assertions.assertThat;

import ch05.books.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest                        // web slice: controllers, advice, filters, WebMvcConfigurers, Jackson
@Import(BookRepository.class)      // @Repository beans are not part of the slice, so add ours
class BookApiTest {

    @Autowired MockMvcTester mvc;  // AssertJ-flavoured MockMvc (Framework 6.2+)
    @Autowired BookRepository repo;

    @BeforeEach
    void reset() { repo.reset(); }

    static void show(String label, MvcTestResult r) throws Exception {
        System.out.println(label + " -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
    }

    @Test
    void listIsPaged() throws Exception {
        MvcTestResult r = mvc.get().uri("/api/books?page=1&size=2").exchange();
        show("GET /api/books?page=1&size=2", r);
        assertThat(r).hasStatusOk()
                .bodyJson().extractingPath("$.content[*].title")
                .asArray().containsExactly("Refactoring", "Domain-Driven Design");
        assertThat(r).bodyJson().extractingPath("$.totalPages").isEqualTo(3);
    }

    @Test
    void getOneShowsJsonShapeAndHeaders() throws Exception {
        MvcTestResult r = mvc.get().uri("/api/books/{id}", 2).exchange();
        show("GET /api/books/2", r);
        System.out.println("  X-Request-Id = " + r.getResponse().getHeader("X-Request-Id"));
        System.out.println("  X-Handler    = " + r.getResponse().getHeader("X-Handler"));
        assertThat(r).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON)
                .hasHeader("X-Handler", "BookController#get");
        assertThat(r).bodyJson().extractingPath("$.published_year").isEqualTo(2018);
        assertThat(r).bodyJson().extractingPath("$.addedOn").isEqualTo("2026-01-15");
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        MvcTestResult r = mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Release It! 2nd ed","author":"Michael T. Nygard",
                         "isbn":"9781680502398","year":2018}""")
                .exchange();
        show("POST /api/books", r);
        System.out.println("  Location = " + r.getResponse().getHeader("Location"));
        assertThat(r).hasStatus(201).hasHeader("Location", "http://localhost/api/books/6");
    }

    @Test
    void updateThenDelete() throws Exception {
        MvcTestResult put = mvc.put().uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Clean Code (2nd ed)","author":"Robert C. Martin","year":2025}""")
                .exchange();                               // no isbn: allowed on update
        show("PUT /api/books/1", put);
        assertThat(put).hasStatusOk().bodyJson().extractingPath("$.isbn").isEqualTo("9780132350884");

        MvcTestResult del = mvc.delete().uri("/api/books/1").exchange();
        show("DELETE /api/books/1", del);
        assertThat(del).hasStatus(204);

        MvcTestResult again = mvc.get().uri("/api/books/1").exchange();
        System.out.println("GET /api/books/1 after delete -> " + again.getResponse().getStatus());
        assertThat(again).hasStatus(404);
    }
}
