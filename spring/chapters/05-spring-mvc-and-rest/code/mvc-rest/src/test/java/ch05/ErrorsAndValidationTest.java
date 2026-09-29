package ch05;

import static org.assertj.core.api.Assertions.assertThat;

import ch05.books.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest
@Import(BookRepository.class)
class ErrorsAndValidationTest {

    @Autowired MockMvcTester mvc;

    static void show(String label, MvcTestResult r) throws Exception {
        System.out.println(label + " -> " + r.getResponse().getStatus()
                + " " + r.getResponse().getContentType() + "\n   " + r.getResponse().getContentAsString());
    }

    @Test
    void unknownBookGivesProblemDetail404() throws Exception {
        MvcTestResult r = mvc.get().uri("/api/books/99").exchange();
        show("GET /api/books/99", r);
        assertThat(r).hasStatus(404).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(r).bodyJson().extractingPath("$.bookId").isEqualTo(99);
    }

    @Test
    void invalidBodyGives400WithFieldErrors() throws Exception {
        MvcTestResult r = mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"","author":"Anon","isbn":"123","year":1200}""")
                .exchange();
        show("POST invalid book", r);
        assertThat(r).hasStatus(400);
        assertThat(r).bodyJson().extractingPath("$.errors.isbn").isEqualTo("must be 13 digits");
        assertThat(r).bodyJson().extractingPath("$.errors.year").isEqualTo("must be greater than or equal to 1450");
    }

    @Test
    void createRequiresIsbnButUpdateDoesNot() throws Exception {
        String noIsbn = """
                {"title":"Some Title","author":"Someone","year":2020}""";
        MvcTestResult create = mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON).content(noIsbn).exchange();
        MvcTestResult update = mvc.put().uri("/api/books/2")
                .contentType(MediaType.APPLICATION_JSON).content(noIsbn).exchange();
        System.out.println("POST without isbn -> " + create.getResponse().getStatus()
                + " errors=" + create.getResponse().getContentAsString().replaceAll(".*\"errors\":", "").replaceAll("}$", ""));
        System.out.println("PUT  without isbn -> " + update.getResponse().getStatus());
        assertThat(create).hasStatus(400);
        assertThat(update).hasStatus(200);
    }

    @Test
    void invalidQueryParameterIsCheckedToo() throws Exception {
        MvcTestResult r = mvc.get().uri("/api/books?size=500").exchange();   // @Max(50) on the parameter
        show("GET /api/books?size=500", r);
        assertThat(r).hasStatus(400);
        assertThat(r).bodyJson().extractingPath("$.errors.size").isEqualTo("must be less than or equal to 50");
    }

    @Test
    void frameworkErrorsAreProblemDetailsToo() throws Exception {
        MvcTestResult wrongType = mvc.get().uri("/api/books?page=abc").exchange();
        MvcTestResult wrongMethod = mvc.patch().uri("/api/books/1").exchange();
        MvcTestResult wrongMedia = mvc.post().uri("/api/books")
                .contentType(MediaType.TEXT_PLAIN).content("hello").exchange();
        MvcTestResult badJson = mvc.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\": ").exchange();
        show("GET /api/books?page=abc", wrongType);
        show("PATCH /api/books/1", wrongMethod);
        show("POST text/plain", wrongMedia);
        show("POST broken JSON", badJson);
        assertThat(wrongType).hasStatus(400);
        assertThat(wrongMethod).hasStatus(405).containsHeader("Allow");
        assertThat(wrongMedia).hasStatus(415);
        assertThat(badJson).hasStatus(400);
    }
}
