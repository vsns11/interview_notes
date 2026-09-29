package ch13.load;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext   // close the server afterwards so its idle threads do not skew the next thread count
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "spring.threads.virtual.enabled=true")
class B_MvcVirtualThreadsLoadTest {

    @LocalServerPort int port;

    @Test
    void load() throws Exception {
        String url = "http://127.0.0.1:" + port + "/io?ms=100";
        String answer = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body();
        System.out.println("MVC, spring.threads.virtual.enabled=true -> \"" + answer + "\"");

        LoadGenerator gen = new LoadGenerator();
        gen.run("warm-up", url, 200, 2000);                                    // JIT + connections
        LoadGenerator.Result r100 = gen.run("MVC Virtual threads", url, 100, 2000);
        LoadGenerator.Result r1000 = gen.run("MVC Virtual threads", url, 1000, 10000);
        System.out.println(LoadGenerator.header());
        System.out.println(r100);
        System.out.println(r1000);
        assertThat(r100.errors() + r1000.errors()).isZero();
    }
}
