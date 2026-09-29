package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@TestPropertySource(properties = "feature.discounts=true")   // different properties -> MISS
class C4_OtherPropertyTest extends BaseIntegrationTest {
    @Autowired MockMvcTester mvc;

    @Test void discounted() {
        assertThat(mvc.get().uri("/greet")).hasBodyTextEqualTo("Hello reader (10% off)");
    }
}
