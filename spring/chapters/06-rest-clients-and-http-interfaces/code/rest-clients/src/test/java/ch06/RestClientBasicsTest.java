package ch06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch06.client.BookDto;
import ch06.client.CatalogClient;
import ch06.client.CatalogUnavailableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class RestClientBasicsTest extends LiveServerTest {

    @Autowired RestClient.Builder builder;     // prototype: each injection is a fresh builder
    @Autowired CatalogClient catalog;
    @Autowired RestTemplateBuilder restTemplateBuilder;

    @Test
    void retrieveBodyAndEntity() {
        RestClient rest = builder.baseUrl(baseUrl()).build();

        BookDto book = rest.get().uri("/catalog/books/{id}", 1).retrieve().body(BookDto.class);
        System.out.println("body(BookDto.class) -> " + book);

        ResponseEntity<List<BookDto>> entity = rest.get()
                .uri(b -> b.path("/catalog/books").queryParam("author", "Martin Fowler").build())
                .retrieve()
                .toEntity(new org.springframework.core.ParameterizedTypeReference<List<BookDto>>() {});
        System.out.println("toEntity(List) -> " + entity.getStatusCode() + " " + entity.getBody().size() + " books");
        assertThat(book.title()).isEqualTo("Release It!");
        assertThat(entity.getBody()).extracting(BookDto::id).containsExactly(2L, 3L);
    }

    @Test
    void errorStatusesBecomeExceptions() {
        RestClient rest = builder.baseUrl(baseUrl()).build();
        assertThatThrownBy(() -> rest.get().uri("/catalog/books/{id}", 999).retrieve().body(BookDto.class))
                .isInstanceOf(HttpClientErrorException.NotFound.class)
                .satisfies(e -> {
                    var ex = (HttpClientErrorException) e;
                    ProblemDetail pd = ex.getResponseBodyAs(ProblemDetail.class);   // read the error body
                    System.out.println("404 -> " + ex.getClass().getSimpleName()
                            + ", problem title='" + pd.getTitle() + "', detail='" + pd.getDetail() + "'");
                });
    }

    @Test
    void handWrittenClient() {
        System.out.println("findBook(2)   -> " + catalog.findBook(2));
        System.out.println("findBook(999) -> " + catalog.findBook(999));
        System.out.println("describe(1)   -> " + catalog.describe(1));
        System.out.println("describe(999) -> " + catalog.describe(999));
        ResponseEntity<BookDto> created = catalog.create("Designing Data-Intensive Applications", "Martin Kleppmann");
        System.out.println("create        -> " + created.getStatusCode() + " Location=" + created.getHeaders().getLocation()
                + " body=" + created.getBody());
        assertThat(catalog.findBook(999)).isEmpty();
        assertThat(created.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void serverErrorsMapToOurOwnException() {
        builder.baseUrl(baseUrl()).build().post().uri("/catalog/flaky/reset").retrieve().toBodilessEntity();
        assertThatThrownBy(() -> catalogFlaky())
                .isInstanceOf(CatalogUnavailableException.class)
                .satisfies(e -> System.out.println("503 -> " + e.getClass().getSimpleName() + ": " + e.getMessage()));
    }

    private String catalogFlaky() {
        // same configuration as CatalogClient: default status handler for 5xx
        RestClient rest = builder.baseUrl(baseUrl())
                .defaultStatusHandler(s -> s.is5xxServerError(), (req, res) -> {
                    throw new CatalogUnavailableException(
                            "catalog answered " + res.getStatusCode().value() + " for " + req.getURI().getPath());
                }).build();
        return rest.get().uri("/catalog/flaky").retrieve().body(String.class);
    }

    @Test
    void interceptorSeesEveryExchange() {
        List<String> log = new ArrayList<>();
        RestClient rest = builder.baseUrl(baseUrl())
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().add("X-Trace-Id", "trace-42");          // add a header
                    var response = execution.execute(request, body);            // do the call
                    log.add(request.getMethod() + " " + request.getURI().getPath() + " -> " + response.getStatusCode().value());
                    return response;
                })
                .build();
        rest.get().uri("/catalog/books/1").retrieve().toBodilessEntity();
        rest.get().uri("/catalog/books/3").retrieve().toBodilessEntity();
        log.forEach(l -> System.out.println("interceptor: " + l));
        assertThat(log).containsExactly("GET /catalog/books/1 -> 200", "GET /catalog/books/3 -> 200");
    }

    @Test
    void readTimeout() {
        var factory = ClientHttpRequestFactoryBuilder.jdk()
                .build(HttpClientSettings.defaults()
                        .withConnectTimeout(Duration.ofSeconds(1))
                        .withReadTimeout(Duration.ofMillis(300)));
        RestClient rest = builder.baseUrl(baseUrl()).requestFactory(factory).build();
        long start = System.nanoTime();
        assertThatThrownBy(() -> rest.get().uri("/catalog/slow").retrieve().body(String.class))
                .isInstanceOf(ResourceAccessException.class)
                .satisfies(e -> System.out.println("slow endpoint -> " + e.getClass().getSimpleName()
                        + " caused by " + e.getCause().getClass().getSimpleName()
                        + " after ~" + ((System.nanoTime() - start) / 100_000_000) * 100 + " ms"));
    }

    @Test
    void legacyRestTemplateStillWorks() {
        RestTemplate template = restTemplateBuilder.rootUri(baseUrl()).build();
        BookDto viaTemplate = template.getForObject("/catalog/books/{id}", BookDto.class, 2);

        RestClient wrapped = RestClient.create(template);     // migration step: reuse its config
        BookDto viaWrapped = wrapped.get().uri("/catalog/books/{id}", 2).retrieve().body(BookDto.class);
        System.out.println("RestTemplate  -> " + viaTemplate);
        System.out.println("RestClient.create(template) -> " + viaWrapped);
        assertThat(viaWrapped).isEqualTo(viaTemplate);
    }
}
