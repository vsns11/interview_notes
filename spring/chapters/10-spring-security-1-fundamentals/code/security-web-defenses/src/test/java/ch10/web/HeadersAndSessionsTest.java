package ch10.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class HeadersAndSessionsTest {

    @Autowired MockMvcTester mvc;

    @Test
    void securityHeaders() {
        var r = mvc.get().uri("/web/home").with(user("alice")).exchange().getResponse();
        System.out.println("response headers on GET /web/home:");
        r.getHeaderNames().stream().sorted().forEach(h -> System.out.println("  " + h + ": " + r.getHeader(h)));
        assertThat(r.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(r.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(r.getHeader("Strict-Transport-Security")).isNull(); // only sent over HTTPS
        var https = mvc.get().uri("https://localhost/web/home").with(user("alice")).exchange().getResponse();
        System.out.println("over HTTPS also: Strict-Transport-Security: " + https.getHeader("Strict-Transport-Security"));
        assertThat(https.getHeader("Strict-Transport-Security")).contains("max-age=31536000");
    }

    @Test
    void statelessApiCreatesNoSession() {
        var r = mvc.get().uri("/api/notes").with(httpBasic("alice", "alice-pass")).exchange();
        System.out.println("API call -> session created? " + (r.getRequest().getSession(false) != null)
                + ", Set-Cookie: " + r.getResponse().getHeader("Set-Cookie"));
        assertThat(r.getRequest().getSession(false)).isNull();
    }

    @Test
    void loginChangesSessionIdAndLogoutEndsIt() {
        MockHttpSession session = new MockHttpSession();       // the anonymous visitor's session
        String before = session.getId();
        var login = mvc.post().uri("/login").session(session)
                .param("username", "alice").param("password", "alice-pass").with(csrf()).exchange();
        MockHttpSession after = (MockHttpSession) login.getRequest().getSession(false);
        System.out.println("login  -> " + login.getResponse().getStatus() + " " + login.getResponse().getRedirectedUrl()
                + ", session id changed: " + !before.equals(after.getId()) + " (session fixation protection)");
        assertThat(after.getId()).isNotEqualTo(before);

        var home = mvc.get().uri("/web/home").session(after).exchange();
        System.out.println("GET /web/home with that session -> " + home.getResponse().getStatus());
        assertThat(home).hasStatusOk();

        var logout = mvc.post().uri("/logout").session(after).with(csrf()).exchange();
        System.out.println("logout -> " + logout.getResponse().getStatus() + " " + logout.getResponse().getRedirectedUrl()
                + ", session invalidated: " + after.isInvalid());
        assertThat(after.isInvalid()).isTrue();
    }
}
