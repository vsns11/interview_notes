package ch05;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest                 // full context: springdoc is not part of the @WebMvcTest slice
@AutoConfigureMockMvc           // Boot 4: @SpringBootTest no longer adds MockMvc on its own
class OpenApiTest {

    @Autowired MockMvcTester mvc;
    @Autowired JsonMapper mapper;

    @Test
    void openApiDocumentIsGenerated() throws Exception {
        MvcTestResult r = mvc.get().uri("/v3/api-docs").exchange();
        assertThat(r).hasStatusOk();
        JsonNode doc = mapper.readTree(r.getResponse().getContentAsString());
        System.out.println("openapi: " + doc.get("openapi").asString());
        doc.get("paths").properties().forEach(e ->
                System.out.println("  " + e.getKey() + " " + e.getValue().propertyNames()));
        assertThat(doc.get("paths").has("/api/books/{id}")).isTrue();
    }
}
