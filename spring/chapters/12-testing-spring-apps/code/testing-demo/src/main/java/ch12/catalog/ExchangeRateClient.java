package ch12.catalog;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls a remote exchange-rate API: GET {base}/rates?from=EUR&to=USD */
@Component
public class ExchangeRateClient {
    private final RestClient http;

    public ExchangeRateClient(RestClient.Builder builder, @Value("${rates.base-url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public BigDecimal rate(String from, String to) {
        ExchangeRate r = http.get()
                .uri("/rates?from={f}&to={t}", from, to)
                .retrieve()
                .body(ExchangeRate.class);
        return r.rate();
    }
}
