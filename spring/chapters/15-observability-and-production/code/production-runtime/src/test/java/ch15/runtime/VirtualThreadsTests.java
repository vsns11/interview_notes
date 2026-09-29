package ch15.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

class VirtualThreadsTests {

    private static String threadOf(RestTestClient client) {
        return client.get().uri("/thread").exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @AutoConfigureRestTestClient
    class Default {
        @Autowired RestTestClient client;

        @Test
        void tomcatUsesItsPlatformThreadPool() {
            String t = threadOf(client);
            System.out.println("default                              -> " + t);
            assertThat(t).startsWith("platform");
        }
    }

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = "spring.threads.virtual.enabled=true")
    @AutoConfigureRestTestClient
    class Virtual {
        @Autowired RestTestClient client;

        @Test
        void eachRequestRunsOnAVirtualThread() {
            String t = threadOf(client);
            System.out.println("spring.threads.virtual.enabled=true  -> " + t);
            assertThat(t).startsWith("virtual");
        }
    }
}
