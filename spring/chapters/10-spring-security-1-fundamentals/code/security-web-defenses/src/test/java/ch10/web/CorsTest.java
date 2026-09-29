package ch10.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    @Autowired MockMvcTester mvc;

    MockHttpServletResponse preflight(String origin) {
        // What the browser sends BEFORE a cross-origin POST with an Authorization header
        return mvc.options().uri("/api/notes")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type")
                .exchange().getResponse();
    }

    @Test
    void preflightFromAllowedOrigin() throws Exception {
        MockHttpServletResponse r = preflight("https://app.example.com");
        System.out.println("preflight from https://app.example.com -> " + r.getStatus());
        for (String h : new String[] {"Access-Control-Allow-Origin", "Access-Control-Allow-Methods",
                "Access-Control-Allow-Headers", "Access-Control-Max-Age"})
            System.out.println("  " + h + ": " + r.getHeader(h));
        assertThat(r.getStatus()).isEqualTo(200);   // no credentials needed for the preflight itself
        assertThat(r.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://app.example.com");
    }

    @Test
    void preflightFromUnknownOrigin() throws Exception {
        MockHttpServletResponse r = preflight("https://evil.example");
        System.out.println("preflight from https://evil.example   -> " + r.getStatus() + " body=" + r.getContentAsString());
        assertThat(r.getStatus()).isEqualTo(403);
        assertThat(r.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void actualCrossOriginRequest() {
        var r = mvc.get().uri("/api/notes").header("Origin", "https://app.example.com")
                .with(httpBasic("alice", "alice-pass")).exchange().getResponse();
        System.out.println("GET with Origin + Basic -> " + r.getStatus()
                + ", Access-Control-Allow-Origin: " + r.getHeader("Access-Control-Allow-Origin")
                + ", Vary: " + r.getHeaders("Vary"));
        assertThat(r.getHeader("Access-Control-Allow-Origin")).isEqualTo("https://app.example.com");
    }
}
