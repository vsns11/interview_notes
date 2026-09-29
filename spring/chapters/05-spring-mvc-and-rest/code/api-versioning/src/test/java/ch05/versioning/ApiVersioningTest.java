package ch05.versioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@WebMvcTest
class ApiVersioningTest {

    @Autowired MockMvcTester mvc;

    MvcTestResult call(String version) {
        var request = mvc.get().uri("/api/greeting");
        if (version != null) request.header("X-API-Version", version);
        return request.exchange();
    }

    @Test
    void routesByVersionHeader() throws Exception {
        for (String v : new String[] {null, "1.0", "1.1", "1.2", "2.0", "3.0"}) {
            MvcTestResult r = call(v);
            System.out.printf("X-API-Version: %-4s -> %d %s%n", v == null ? "none" : v,
                    r.getResponse().getStatus(), r.getResponse().getContentAsString());
        }
        assertThat(call(null)).hasStatusOk().bodyJson().extractingPath("$.message").isEqualTo("Hello");
        assertThat(call("1.2")).hasStatusOk().bodyJson().extractingPath("$.language").isEqualTo("en");
        assertThat(call("2.0")).hasStatusOk().bodyJson().extractingPath("$.text").isEqualTo("Hello");
        assertThat(call("3.0")).hasStatus(400);
    }

    @Test
    void deprecatedVersionGetsStandardHeaders() {
        MvcTestResult r = call("1.0");
        for (String h : new String[] {"Deprecation", "Sunset", "Link"}) {
            System.out.println(h + ": " + r.getResponse().getHeader(h));
        }
        assertThat(r).containsHeader("Deprecation").containsHeader("Sunset");
        assertThat(call("2.0")).doesNotContainHeader("Deprecation");
    }
}
