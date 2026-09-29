package ch10.defaults;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class DefaultsTest {

    @Autowired MockMvcTester mvc;

    @Test
    void everythingIsLockedByDefault(CapturedOutput startupLog) throws Exception {
        // 1. Boot generated a user called "user" and logged its random password at startup
        String line = startupLog.getAll().lines()
                .filter(l -> l.contains("Using generated security password")).findFirst().orElseThrow();
        String password = line.substring(line.indexOf(':') + 1).trim();
        System.out.println("startup log   : Using generated security password: <" + password.length() + "-char UUID>");

        // 2. A browser-style request without credentials is redirected to the generated login page
        MvcTestResult browser = mvc.get().uri("/hello").header(HttpHeaders.ACCEPT, "text/html").exchange();
        System.out.println("browser, anon : " + browser.getResponse().getStatus()
                + " -> " + browser.getResponse().getRedirectedUrl());
        assertThat(browser).hasStatus(302).hasRedirectedUrl("/login");

        // 3. An API-style request gets 401 plus a Basic challenge
        MvcTestResult api = mvc.get().uri("/hello").header(HttpHeaders.ACCEPT, "application/json").exchange();
        System.out.println("api, anon     : " + api.getResponse().getStatus()
                + " WWW-Authenticate=" + api.getResponse().getHeader("WWW-Authenticate"));
        assertThat(api).hasStatus(401);

        // 4. With the generated credentials it works
        MvcTestResult ok = mvc.get().uri("/hello").with(httpBasic("user", password)).exchange();
        System.out.println("basic, user   : " + ok.getResponse().getStatus() + " body=" + ok.getResponse().getContentAsString());
        assertThat(ok).hasStatusOk().hasBodyTextEqualTo("hello");
    }
}
