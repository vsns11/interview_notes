package ch11.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** Steps 1-3 of the authorization-code flow run for real; the provider's side is out of scope. */
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2LoginTest {

    @Autowired MockMvcTester mvc;

    @Test
    void redirectsToTheProvider() {
        var step1 = mvc.get().uri("/profile").header("Accept", "text/html").exchange();
        System.out.println("1. GET /profile (anonymous)       -> " + step1.getResponse().getStatus()
                + " " + step1.getResponse().getRedirectedUrl());
        assertThat(step1.getResponse().getRedirectedUrl()).isEqualTo("/oauth2/authorization/demo-idp");

        var step2 = mvc.get().uri("/oauth2/authorization/demo-idp").exchange();
        String location = step2.getResponse().getRedirectedUrl();
        URI uri = URI.create(location);
        System.out.println("2. GET /oauth2/authorization/demo-idp -> " + step2.getResponse().getStatus()
                + " to " + uri.getScheme() + "://" + uri.getHost() + uri.getPath());
        List<String> params = Arrays.stream(uri.getRawQuery().split("&")).sorted().toList();
        for (String p : params) {
            String masked = p.matches("(state|nonce|code_challenge)=.*") ? p.substring(0, p.indexOf('=') + 1) + "<random>" : p;
            System.out.println("     " + masked);
        }
        assertThat(location).startsWith("https://idp.example.test/oauth2/authorize?")
                .contains("response_type=code", "client_id=notes-web", "state=", "nonce=");
        // The pending request (with state) is remembered in the HTTP session for the callback
        System.out.println("   session attribute saved: " + (step2.getRequest().getSession(false) != null));
    }

    @Test
    void callbackWithUnknownStateIsRejected() {
        // An attacker (or a stale tab) returns a code with a state this browser never requested
        var r = mvc.get().uri("/login/oauth2/code/demo-idp?code=abc&state=forged").exchange();
        System.out.println("3. callback with forged state      -> " + r.getResponse().getStatus()
                + " " + r.getResponse().getRedirectedUrl());
        assertThat(r.getResponse().getRedirectedUrl()).isEqualTo("/login?error");
    }

    @Test
    void oidcLoginPostProcessor() throws Exception {
        var r = mvc.get().uri("/profile").with(oidcLogin().idToken(t -> t
                .subject("248289761001").claim("name", "Jane Doe").claim("email", "jane@example.com")
                .issuer("https://idp.example.test"))).exchange();
        System.out.println("oidcLogin() GET /profile -> " + r.getResponse().getStatus() + " " + r.getResponse().getContentAsString());
        assertThat(r).hasStatusOk();
    }
}
