package ch11.variants;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class MultiTenantTest {

    @Autowired MockMvcTester mvc;
    @Autowired TenantKeys keys;

    String token(JwtEncoder signer, String issuer, String subject) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject(subject)
                .issuedAt(now).expiresAt(now.plusSeconds(300)).build();
        return signer.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
    }

    MockHttpServletResponse whoami(String token) {
        return mvc.get().uri("/tenant/whoami").header("Authorization", "Bearer " + token).exchange().getResponse();
    }

    @Test
    void issuerSelectsTheKeys() throws Exception {
        var a = whoami(token(keys.encoderFor(TenantKeys.TENANT_A), TenantKeys.TENANT_A, "ann"));
        var b = whoami(token(keys.encoderFor(TenantKeys.TENANT_B), TenantKeys.TENANT_B, "ben"));
        // tenant B's key, but the token CLAIMS to come from tenant A
        var spoof = whoami(token(keys.encoderFor(TenantKeys.TENANT_B), TenantKeys.TENANT_A, "mallory"));
        var unknown = whoami(token(keys.encoderFor(TenantKeys.TENANT_A), "https://idp.unknown.test", "eve"));

        System.out.println("tenant A token            -> " + a.getStatus() + " " + a.getContentAsString());
        System.out.println("tenant B token            -> " + b.getStatus() + " " + b.getContentAsString());
        System.out.println("B-signed, iss = tenant A  -> " + spoof.getStatus());
        System.out.println("untrusted issuer          -> " + unknown.getStatus());
        assertThat(a.getStatus()).isEqualTo(200);
        assertThat(b.getStatus()).isEqualTo(200);
        assertThat(spoof.getStatus()).isEqualTo(401);
        assertThat(unknown.getStatus()).isEqualTo(401);
    }
}
