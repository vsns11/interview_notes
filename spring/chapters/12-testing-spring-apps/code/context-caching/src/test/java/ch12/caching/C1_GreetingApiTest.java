package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

class C1_GreetingApiTest extends BaseIntegrationTest {
    @Autowired MockMvcTester mvc;

    @Test void greets() {
        assertThat(mvc.get().uri("/greet")).hasStatusOk().hasBodyTextEqualTo("Hello reader");
    }
}
