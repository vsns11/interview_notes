package ch13.load;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext   // close the server afterwards so its idle threads do not skew the next thread count
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class C_WebfluxLoadTest {

    @LocalServerPort int port;

    @Test
    void load() throws Exception {
        String base = "http://127.0.0.1:" + port;
        System.out.println("CPU cores = " + Runtime.getRuntime().availableProcessors()
                + " (Reactor Netty event-loop threads default to this number)");

        LoadGenerator gen = new LoadGenerator();
        gen.run("warm-up", base + "/io?ms=100", 200, 2000);
        LoadGenerator.Result r100 = gen.run("WebFlux non-blocking", base + "/io?ms=100", 100, 2000);
        LoadGenerator.Result r1000 = gen.run("WebFlux non-blocking", base + "/io?ms=100", 1000, 10000);
        LoadGenerator.Result bad = gen.run("WebFlux + Thread.sleep (bad)", base + "/io-blocking?ms=100", 100, 500);
        System.out.println(LoadGenerator.header());
        System.out.println(r100);
        System.out.println(r1000);
        System.out.println(bad);
        assertThat(r100.errors() + r1000.errors() + bad.errors()).isZero();
    }
}
