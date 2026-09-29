package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

class OpenInViewTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class DefaultOn {
        @Autowired MockMvcTester mvc;

        @Test
        void lazyLoadingInTheControllerWorks() throws Exception {
            var result = mvc.get().uri("/authors/2/summary").exchange();
            System.out.println("open-in-view=true  -> " + result.getResponse().getStatus() + " "
                    + result.getResponse().getContentAsString());
            assertThat(result).hasStatusOk();
        }
    }

    @Nested
    @SpringBootTest(properties = "spring.jpa.open-in-view=false")
    @AutoConfigureMockMvc
    class TurnedOff {
        @Autowired MockMvcTester mvc;

        @Test
        void lazyLoadingInTheControllerFails() {
            var result = mvc.get().uri("/authors/2/summary").exchange();
            assertThat(result).hasFailed().failure()
                    .hasRootCauseInstanceOf(LazyInitializationException.class)
                    .satisfies(e -> System.out.println("open-in-view=false -> " + e.getCause().getMessage()));
        }
    }
}
