package ch05;

import static org.assertj.core.api.Assertions.assertThat;

import ch05.books.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest
@Import(BookRepository.class)
class NegotiationCorsUploadTest {

    @Autowired MockMvcTester mvc;

    @Test
    void acceptHeaderPicksTheRepresentation() throws Exception {
        MvcTestResult csv = mvc.get().uri("/api/books/export").accept(MediaType.parseMediaType("text/csv")).exchange();
        MvcTestResult json = mvc.get().uri("/api/books/export").accept(MediaType.APPLICATION_JSON).exchange();
        MvcTestResult xml = mvc.get().uri("/api/books/export").accept(MediaType.APPLICATION_XML).exchange();
        System.out.println("Accept: text/csv -> " + csv.getResponse().getContentType() + "\n"
                + csv.getResponse().getContentAsString().lines().limit(3).map(l -> "   " + l).reduce((a, b) -> a + "\n" + b).orElse(""));
        System.out.println("Accept: application/json -> " + json.getResponse().getContentType()
                + " starts " + json.getResponse().getContentAsString().substring(0, 40) + "...");
        System.out.println("Accept: application/xml -> " + xml.getResponse().getStatus());
        assertThat(csv).hasStatusOk().hasContentType("text/csv;charset=UTF-8");
        assertThat(json).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
        assertThat(xml).hasStatus(406);
    }

    @Test
    void corsPreflightFromAllowedAndUnknownOrigins() {
        MvcTestResult ok = mvc.options().uri("/api/books")
                .header(HttpHeaders.ORIGIN, "https://shop.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST").exchange();
        MvcTestResult evil = mvc.options().uri("/api/books")
                .header(HttpHeaders.ORIGIN, "https://evil.example.net")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST").exchange();
        System.out.println("preflight from shop.example.com -> " + ok.getResponse().getStatus()
                + " Allow-Origin=" + ok.getResponse().getHeader("Access-Control-Allow-Origin")
                + " Allow-Methods=" + ok.getResponse().getHeader("Access-Control-Allow-Methods")
                + " Max-Age=" + ok.getResponse().getHeader("Access-Control-Max-Age"));
        System.out.println("preflight from evil.example.net -> " + evil.getResponse().getStatus()
                + " Allow-Origin=" + evil.getResponse().getHeader("Access-Control-Allow-Origin"));
        assertThat(ok).hasStatusOk().hasHeader("Access-Control-Allow-Origin", "https://shop.example.com");
        assertThat(evil).hasStatus(403);
    }

    @Test
    void multipartUpload() throws Exception {
        var file = new MockMultipartFile("file", "books.csv", "text/csv",
                "id,title\n1,Clean Code\n2,Refactoring\n".getBytes());
        MvcTestResult r = mvc.post().uri("/api/uploads").multipart()
                .file(file).param("note", "weekly import").exchange();
        System.out.println("POST /api/uploads -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        assertThat(r).hasStatusOk().bodyJson().extractingPath("$.lines").isEqualTo(3);
    }
}
