package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Plain unit test - no Spring context needed to study password encoders. */
class PasswordEncodingTest {

    static String cut(String s) { return s.length() > 60 ? s.substring(0, 60) + "..." : s; }

    @Test
    void delegatingEncoderFormat() {
        PasswordEncoder enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String h1 = enc.encode("s3cret");
        String h2 = enc.encode("s3cret");
        System.out.println("encode #1 : " + h1);
        System.out.println("encode #2 : " + h2);
        System.out.println("same input, different hash (random salt): " + !h1.equals(h2));
        System.out.println("matches(s3cret) = " + enc.matches("s3cret", h1) + ", matches(S3cret) = " + enc.matches("S3cret", h1));
        assertThat(h1).startsWith("{bcrypt}$2a$10$").hasSize(8 + 60).isNotEqualTo(h2);
        assertThat(enc.matches("s3cret", h1)).isTrue();

        // The same encoder can verify hashes written by other algorithms, thanks to the {id} prefix
        String argon = "{argon2@SpringSecurity_v5_8}" + Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("s3cret");
        System.out.println("argon2 row: " + cut(argon));
        System.out.println("matches(argon2 row) = " + enc.matches("s3cret", argon));
        assertThat(enc.matches("s3cret", argon)).isTrue();
        assertThat(enc.matches("s3cret", "{noop}s3cret")).isTrue();

        // upgradeEncoding(): true if the stored hash is not the current default
        System.out.println("upgradeEncoding({noop})  = " + enc.upgradeEncoding("{noop}s3cret"));
        System.out.println("upgradeEncoding(argon2)  = " + enc.upgradeEncoding(argon));
        System.out.println("upgradeEncoding(bcrypt)  = " + enc.upgradeEncoding(h1));
        assertThat(enc.upgradeEncoding("{noop}s3cret")).isTrue();
        assertThat(enc.upgradeEncoding(h1)).isFalse();
    }

    @Test
    void argon2AsTheDefaultForNewHashes() {
        // Your own DelegatingPasswordEncoder: argon2 for new hashes, still reads bcrypt and legacy rows
        @SuppressWarnings("deprecation")
        Map<String, PasswordEncoder> known = Map.of(
                "argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
                "bcrypt", new BCryptPasswordEncoder(),
                "noop", NoOpPasswordEncoder.getInstance());
        var enc = new DelegatingPasswordEncoder("argon2@SpringSecurity_v5_8", known);
        String hash = enc.encode("s3cret");
        System.out.println("argon2 default: " + cut(hash));
        String oldBcrypt = "{bcrypt}" + new BCryptPasswordEncoder().encode("s3cret");
        System.out.println("old bcrypt row still matches: " + enc.matches("s3cret", oldBcrypt)
                + ", needs upgrade: " + enc.upgradeEncoding(oldBcrypt));
        assertThat(hash).startsWith("{argon2@SpringSecurity_v5_8}$argon2id$v=19$m=16384,t=2,p=1$");
        assertThat(enc.upgradeEncoding(oldBcrypt)).isTrue();
    }

    @Test
    void bcryptCostFactor() {
        // strength = log2(rounds). Each +1 doubles the work.
        for (int strength : new int[] {10, 12}) {
            var bc = new BCryptPasswordEncoder(strength);
            String h = bc.encode("warm-up");
            long t0 = System.nanoTime();
            for (int i = 0; i < 5; i++) bc.matches("s3cret", h);
            long ms = (System.nanoTime() - t0) / 5_000_000;
            System.out.println("bcrypt strength " + strength + " -> " + h.substring(0, 7) + "...  ~" + ms + " ms per check");
        }
    }

    @Test
    void bcryptHas72ByteLimit() {
        var bc = new BCryptPasswordEncoder();
        String longPassword = "a".repeat(73);
        assertThatThrownBy(() -> bc.encode(longPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .satisfies(e -> System.out.println("73-byte password -> IllegalArgumentException: " + e.getMessage()));
    }
}
