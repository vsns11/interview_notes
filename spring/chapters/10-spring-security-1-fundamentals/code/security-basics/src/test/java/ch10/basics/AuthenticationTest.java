package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTest {

    @Autowired MockMvcTester mvc;
    @Autowired MockMvc mockMvc; // for the formLogin() request builder

    private static void show(String label, MvcTestResult r) throws Exception {
        var res = r.getResponse();
        String extra = res.getRedirectedUrl() != null ? " -> " + res.getRedirectedUrl()
                : res.getHeader("WWW-Authenticate") != null ? " WWW-Authenticate=" + res.getHeader("WWW-Authenticate")
                : " " + res.getContentAsString();
        System.out.printf("%-28s %d%s%n", label, res.getStatus(), extra);
    }

    @Test
    void basicAuthentication() throws Exception {
        MvcTestResult noCreds = mvc.get().uri("/me").header("Accept", "application/json").exchange();
        MvcTestResult wrong = mvc.get().uri("/me").with(httpBasic("alice", "nope")).exchange();
        MvcTestResult disabled = mvc.get().uri("/me").with(httpBasic("erin", "erin-pass")).exchange();
        MvcTestResult ok = mvc.get().uri("/me").with(httpBasic("alice", "alice-pass")).exchange();
        show("no credentials (JSON)", noCreds);
        show("alice, wrong password", wrong);
        show("erin, disabled account", disabled);
        show("alice, right password", ok);
        assertThat(noCreds).hasStatus(401);
        assertThat(wrong).hasStatus(401);
        assertThat(disabled).hasStatus(401);
        assertThat(ok).hasStatusOk().bodyJson().extractingPath("$.authorities").asArray()
                .containsExactly("FACTOR_PASSWORD", "ROLE_USER", "report:read"); // FACTOR_*: Security 7 MFA
    }

    @Test
    void browserGetsRedirectedToLoginPage() throws Exception {
        MvcTestResult r = mvc.get().uri("/me").header("Accept", "text/html").exchange();
        show("no credentials (HTML)", r);
        assertThat(r).hasStatus(302).hasRedirectedUrl("/login");
    }

    @Test
    void formLoginFlow() throws Exception {
        // formLogin() builds POST /login with username, password AND a valid CSRF token
        var good = mockMvc.perform(formLogin().user("alice").password("alice-pass"))
                .andExpect(authenticated().withUsername("alice")).andReturn();
        var bad = mockMvc.perform(formLogin().user("alice").password("wrong"))
                .andExpect(unauthenticated()).andReturn();
        System.out.println("form login, good password   " + good.getResponse().getStatus() + " -> " + good.getResponse().getRedirectedUrl());
        System.out.println("form login, bad password    " + bad.getResponse().getStatus() + " -> " + bad.getResponse().getRedirectedUrl());
        assertThat(good.getResponse().getRedirectedUrl()).isEqualTo("/");
        assertThat(bad.getResponse().getRedirectedUrl()).isEqualTo("/login?error");
    }
}
