package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordUpgradeTest {

    @Autowired MockMvcTester mvc;
    @Autowired AccountUserDetailsService accounts;

    @Test
    void legacyHashIsUpgradedOnSuccessfulLogin() {
        System.out.println("before login: " + accounts.find("dave").passwordHash());
        assertThat(mvc.get().uri("/me").with(httpBasic("dave", "dave-pass"))).hasStatusOk();
        String after = accounts.find("dave").passwordHash();
        System.out.println("after login : " + after.substring(0, 22) + "...");
        assertThat(after).startsWith("{bcrypt}$2a$10$");
    }
}
