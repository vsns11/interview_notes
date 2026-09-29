package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Sheets 19.5 and 19.8: Jackson 3 defaults in Boot 4, roles vs authorities, password encoding. */
class JsonAndSecurityDefaultsTests {

    record Delivery(String item, LocalDate on) { }

    @Test
    void jackson3Defaults() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .run(ctx -> {
                    JsonMapper json = ctx.getBean(JsonMapper.class);
                    String written = json.writeValueAsString(new Delivery("book", LocalDate.of(2026, 9, 28)));
                    Delivery read = json.readValue("{\"item\":\"pen\",\"on\":\"2026-10-01\",\"unknown\":42}", Delivery.class);
                    System.out.println("JsonMapper (" + json.getClass().getPackageName() + ") writes " + written);
                    System.out.println("unknown property ignored -> " + read);
                    assertThat(written).isEqualTo("{\"item\":\"book\",\"on\":\"2026-09-28\"}");
                    assertThat(read.on()).isEqualTo(LocalDate.of(2026, 10, 1));
                    assertThatThrownBy(() -> json.readValue("{broken", Delivery.class))
                            .isInstanceOf(JacksonException.class).isInstanceOf(RuntimeException.class);
                    System.out.println("broken JSON -> JacksonException (unchecked)");
                });
    }

    @Test
    void hasRoleMeansRolePrefix() {
        var hasRoleAdmin = AuthorityAuthorizationManager.hasRole("ADMIN");
        var role = new TestingAuthenticationToken("bob", "pw", "ROLE_ADMIN");
        var plain = new TestingAuthenticationToken("eve", "pw", "ADMIN");
        boolean roleGranted = hasRoleAdmin.authorize(() -> role, new Object()).isGranted();
        boolean plainGranted = hasRoleAdmin.authorize(() -> plain, new Object()).isGranted();
        System.out.println("hasRole(\"ADMIN\"): authority ROLE_ADMIN -> " + roleGranted + ", authority ADMIN -> " + plainGranted);
        assertThat(roleGranted).isTrue();
        assertThat(plainGranted).isFalse();
        assertThatThrownBy(() -> AuthorityAuthorizationManager.hasRole("ROLE_ADMIN"))
                .isInstanceOf(IllegalArgumentException.class);
        System.out.println("hasRole(\"ROLE_ADMIN\") -> IllegalArgumentException");
    }

    @Test
    void delegatingPasswordEncoder() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String hash = encoder.encode("s3cret");
        System.out.println("encode -> " + hash.substring(0, 15) + "...  matches {noop}s3cret? "
                + encoder.matches("s3cret", "{noop}s3cret") + "  upgradeEncoding({noop}) = " + encoder.upgradeEncoding("{noop}s3cret"));
        assertThat(hash).startsWith("{bcrypt}$2a$10$");
        assertThat(encoder.matches("s3cret", hash)).isTrue();
        assertThat(encoder.upgradeEncoding("{noop}s3cret")).isTrue();
    }
}
