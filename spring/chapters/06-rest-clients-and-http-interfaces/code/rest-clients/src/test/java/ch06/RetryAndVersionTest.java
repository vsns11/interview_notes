package ch06;

import static org.assertj.core.api.Assertions.assertThat;

import ch06.client.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;

class RetryAndVersionTest extends LiveServerTest {

    @Autowired StockService stock;
    @Autowired RestClient.Builder builder;

    @Test
    void retryableRidesOutTwo503s() {
        builder.baseUrl(baseUrl()).build().post().uri("/catalog/flaky/reset").retrieve().toBodilessEntity();
        String result = stock.checkStock();          // 503, 503, then 200 - retried transparently
        System.out.println("checkStock() -> " + result);
        assertThat(result).isEqualTo("ok on call 3");
    }

    @Test
    void clientSideApiVersioning() {
        RestClient rest = builder.baseUrl(baseUrl())
                .apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))  // how to send it
                .defaultApiVersion("1.0")                                           // unless overridden
                .build();
        String v1 = rest.get().uri("/catalog/greeting").retrieve().body(String.class);
        String v2 = rest.get().uri("/catalog/greeting").apiVersion("2.0").retrieve().body(String.class);
        System.out.println("default version -> " + v1);
        System.out.println("apiVersion(2.0) -> " + v2);
        assertThat(v1).isEqualTo("v1: hello");
        assertThat(v2).isEqualTo("v2: hello");
    }
}
