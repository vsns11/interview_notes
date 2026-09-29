package ch09.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class DataRestTests {

    @Autowired MockMvcTester mvc;

    @Test
    void repositoriesBecomeRestEndpoints() throws Exception {
        var one = mvc.get().uri("/api/customers/2").exchange();
        System.out.println("GET /api/customers/2 -> " + one.getResponse().getStatus());
        System.out.println(one.getResponse().getContentAsString());

        var search = mvc.get().uri("/api/customers/search/findByName?name=Asha").exchange();
        System.out.println("GET /api/customers/search/findByName?name=Asha -> " + search.getResponse().getStatus());

        var products = mvc.get().uri("/api/products").exchange();
        System.out.println("GET /api/products -> " + products.getResponse().getStatus() + " (exported = false)");

        assertThat(one).hasStatusOk().bodyJson().extractingPath("$.email").isEqualTo("ben@example.com");
        assertThat(search).hasStatusOk();
        assertThat(products).hasStatus(404);
    }
}
