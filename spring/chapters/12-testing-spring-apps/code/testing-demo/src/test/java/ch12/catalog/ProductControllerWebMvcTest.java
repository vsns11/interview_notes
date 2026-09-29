package ch12.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Web slice: only the MVC layer is real; the service is a Mockito mock. */
@WebMvcTest(ProductController.class)
class ProductControllerWebMvcTest {

    @Autowired MockMvcTester mvc;
    @Autowired ApplicationContext ctx;
    @MockitoBean PriceService service;       // replaces (or adds) the bean in the test context

    @Test
    void sliceContainsOnlyWebBeans() {
        System.out.println("@WebMvcTest beans: " + ctx.getBeanDefinitionCount()
                + ", ProductController present: " + (ctx.getBeanNamesForType(ProductController.class).length == 1)
                + ", ProductRepository present: " + (ctx.getBeanNamesForType(ProductRepository.class).length == 1));
        assertThat(ctx.getBeanNamesForType(ProductRepository.class)).isEmpty();
    }

    @Test
    void returnsPriceAsJson() throws Exception {
        given(service.priceOf("MUG-001", "USD"))
                .willReturn(new PriceView("MUG-001", "Mug", new BigDecimal("10.85"), "USD"));

        MvcTestResult r = mvc.get().uri("/api/products/{sku}/price?currency=USD", "MUG-001").exchange();

        System.out.println("GET price -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        assertThat(r).hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().extractingPath("$.amount").isEqualTo(10.85);
    }

    @Test
    void unknownSkuIsProblemDetail404() throws Exception {
        given(service.priceOf("NOPE-000", "EUR")).willThrow(new ProductNotFoundException("NOPE-000"));

        MvcTestResult r = mvc.get().uri("/api/products/NOPE-000/price").exchange();

        System.out.println("GET unknown -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        assertThat(r).hasStatus(404)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.detail").isEqualTo("No product with SKU NOPE-000");
    }

    @Test
    void invalidBodyIs400AndServiceIsNeverCalled() {
        MvcTestResult r = mvc.post().uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku":"mug1","name":"","price":0}""")
                .exchange();

        System.out.println("POST invalid -> " + r.getResponse().getStatus());
        assertThat(r).hasStatus(400);
        verifyNoInteractions(service);
    }

    @Test
    void validBodyIs201WithLocation() {
        given(service.create(any())).willReturn(new Product("MUG-002", "Big mug", new BigDecimal("12.50")));

        MvcTestResult r = mvc.post().uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku":"MUG-002","name":"Big mug","price":12.50}""")
                .exchange();

        System.out.println("POST valid -> " + r.getResponse().getStatus() + " Location=" + r.getResponse().getHeader("Location"));
        assertThat(r).hasStatus(201).hasHeader("Location", "/api/products/MUG-002/price");
    }
}
