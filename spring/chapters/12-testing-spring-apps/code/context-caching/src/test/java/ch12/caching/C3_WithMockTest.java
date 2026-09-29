package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

class C3_WithMockTest extends BaseIntegrationTest {         // a @MockitoBean changes the key -> MISS
    @Autowired MockMvcTester mvc;
    @MockitoBean CachingApplication.GreetingService service;

    @Test void mocked() {
        given(service.greet("reader")).willReturn("Hi from a mock");
        assertThat(mvc.get().uri("/greet")).hasBodyTextEqualTo("Hi from a mock");
    }
}
