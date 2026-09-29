package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {

    @Autowired MockMvcTester mvc;

    int status(String url, String user) {
        var req = mvc.get().uri(url).header("Accept", "application/json");
        if (user != null) req = req.with(httpBasic(user, user + "-pass"));
        return req.exchange().getResponse().getStatus();
    }

    @Test
    void roleVersusAuthorityMatrix() {
        List<String> urls = List.of("/public/hello", "/user/profile", "/admin/stats", "/reports/daily", "/nowhere");
        Map<String, String> users = new LinkedHashMap<>();
        users.put("anonymous", null);
        users.put("alice (ROLE_USER, report:read)", "alice");
        users.put("carol (ROLE_USER)", "carol");
        users.put("bob (ROLE_ADMIN)", "bob");

        System.out.printf("%-31s", "");
        urls.forEach(u -> System.out.printf("%-15s", u));
        System.out.println();
        users.forEach((label, user) -> {
            System.out.printf("%-31s", label);
            urls.forEach(u -> System.out.printf("%-15d", status(u, user)));
            System.out.println();
        });

        assertThat(status("/admin/stats", null)).isEqualTo(401);    // who are you?
        assertThat(status("/admin/stats", "carol")).isEqualTo(403); // I know you, but no
        assertThat(status("/reports/daily", "alice")).isEqualTo(200); // authority, not role
        assertThat(status("/reports/daily", "bob")).isEqualTo(403);   // ADMIN role is not report:read
        assertThat(status("/user/profile", "bob")).isEqualTo(200);    // role hierarchy ADMIN > USER
        assertThat(status("/nowhere", "bob")).isEqualTo(403);         // denyAll catch-all
    }

    @Test
    void hasRoleAddsThePrefixForYou() {
        // hasRole("X") means hasAuthority("ROLE_X"), so passing "ROLE_X" is rejected
        assertThatThrownBy(() -> AuthorityAuthorizationManager.hasRole("ROLE_ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> System.out.println("hasRole(\"ROLE_ADMIN\") -> " + e.getMessage()));
        assertThatThrownBy(() -> User.withUsername("x").password("p").roles("ROLE_ADMIN").build())
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> System.out.println("User.roles(\"ROLE_ADMIN\") -> " + e.getMessage()));
    }
}
