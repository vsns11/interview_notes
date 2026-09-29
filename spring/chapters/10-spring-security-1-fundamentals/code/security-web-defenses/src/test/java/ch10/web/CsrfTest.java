package ch10.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class CsrfTest {

    @Autowired MockMvcTester mvc;

    @Test
    void csrfOnForTheCookieApp() throws Exception {
        int noToken = mvc.post().uri("/web/notes").param("text", "hi")
                .with(user("alice")).exchange().getResponse().getStatus();
        int badToken = mvc.post().uri("/web/notes").param("text", "hi")
                .with(user("alice")).with(csrf().useInvalidToken()).exchange().getResponse().getStatus();
        int goodToken = mvc.post().uri("/web/notes").param("text", "hi")
                .with(user("alice")).with(csrf()).exchange().getResponse().getStatus();
        int getNoToken = mvc.get().uri("/web/home").with(user("alice")).exchange().getResponse().getStatus();
        System.out.println("POST /web/notes, logged in, no CSRF token  -> " + noToken);
        System.out.println("POST /web/notes, logged in, wrong token    -> " + badToken);
        System.out.println("POST /web/notes, logged in, valid token    -> " + goodToken);
        System.out.println("GET  /web/home,  no token needed (safe)     -> " + getNoToken);
        assertThat(noToken).isEqualTo(403);
        assertThat(badToken).isEqualTo(403);
        assertThat(goodToken).isEqualTo(200);
        assertThat(getNoToken).isEqualTo(200);
    }

    @Test
    void loginPageEmbedsTheToken() throws Exception {
        String html = mvc.get().uri("/login").exchange().getResponse().getContentAsString();
        String input = html.lines().filter(l -> l.contains("_csrf")).findFirst().orElseThrow().trim();
        System.out.println("login page hidden field: " + input.replaceAll("value=\"[^\"]+\"", "value=\"<token>\""));
        assertThat(input).contains("name=\"_csrf\"");
    }

    @Test
    void csrfOffForTheStatelessApi() {
        var r = mvc.post().uri("/api/notes").contentType(MediaType.TEXT_PLAIN).content("from curl")
                .with(httpBasic("alice", "alice-pass")).exchange();
        System.out.println("POST /api/notes, Basic auth, no CSRF token -> " + r.getResponse().getStatus());
        assertThat(r).hasStatus(201);
    }
}
