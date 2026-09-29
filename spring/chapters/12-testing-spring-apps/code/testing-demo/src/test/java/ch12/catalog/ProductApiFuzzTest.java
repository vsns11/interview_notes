package ch12.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * A tiny fuzz test (§12.18): throw 500 random, broken and hostile request bodies and paths at the web slice
 * and assert the API never answers 5xx. Same configuration as ProductControllerWebMvcTest, so the cached
 * context is reused. The seed is fixed so a failure can be replayed.
 */
@WebMvcTest(ProductController.class)
class ProductApiFuzzTest {

    @Autowired MockMvcTester mvc;
    @MockitoBean PriceService service;

    static final List<String> SKUS = List.of("\"MUG-001\"", "\"mug-001\"", "\"\"", "null", "42", "\"MUG-0001\"",
            "\"' OR 1=1 --\"", "\"<script>alert(1)</script>\"", "\"\\u0000\"", "\"" + "A".repeat(5000) + "\"", "[]", "{}");
    static final List<String> NAMES = List.of("\"Mug\"", "\"\"", "null", "\"   \"", "\"Ünïcødé ☕\"", "123", "true", "{\"x\":1}");
    static final List<String> PRICES = List.of("9.99", "0", "-1", "0.001", "\"9.99\"", "\"abc\"", "null", "1e400",
            "99999999999999999999999999999999.99", "NaN", "[]", "true");
    static final List<String> JUNK = List.of("", "{", "}", "{\"sku\":", "[1,2,3]", "\"just a string\"", "null",
            "{\"sku\":\"MUG-001\",\"name\":\"Mug\",\"price\":9.99", "{" + "\"a\":{".repeat(200) + "}".repeat(201));

    @Test
    void noRandomInputCausesA5xx() throws Exception {
        given(service.create(any())).willAnswer(inv -> {
            NewProduct p = inv.getArgument(0);
            return new Product(p.sku(), p.name(), p.price());
        });
        given(service.priceOf(anyString(), anyString())).willThrow(new ProductNotFoundException("x"));

        Random rnd = new Random(12);
        Map<Integer, Integer> statuses = new TreeMap<>();
        for (int i = 0; i < 500; i++) {
            int status;
            if (i % 5 == 4) {                                           // random path + query parameter
                String sku = randomPathSegment(rnd);
                status = mvc.get().uri("/api/products/{sku}/price?currency={c}", sku, randomPathSegment(rnd))
                        .exchange().getResponse().getStatus();
            } else {
                String body = rnd.nextInt(6) == 0 ? pick(rnd, JUNK)
                        : "{\"sku\":%s,\"name\":%s,\"price\":%s}".formatted(pick(rnd, SKUS), pick(rnd, NAMES), pick(rnd, PRICES));
                status = mvc.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).content(body)
                        .exchange().getResponse().getStatus();
            }
            statuses.merge(status, 1, Integer::sum);
            assertThat(status).as("request %d", i).isLessThan(500);
        }
        System.out.println("fuzz: 500 requests, status histogram " + statuses);
    }

    static String pick(Random r, List<String> l) { return l.get(r.nextInt(l.size())); }

    static String randomPathSegment(Random r) {
        String alphabet = "ABCXYZ019-_.~%é☕ <>\"'";
        StringBuilder sb = new StringBuilder();
        for (int n = r.nextInt(12); n >= 0; n--) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }
}
