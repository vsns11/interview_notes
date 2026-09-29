package ch11.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import tools.jackson.databind.json.JsonMapper;

/** Real tokens, real signature checks: nothing is mocked here. */
@SpringBootTest
@AutoConfigureMockMvc
class JwtResourceServerTest {

    @Autowired MockMvcTester mvc;
    @Autowired JwtEncoder encoder;
    final JsonMapper json = JsonMapper.builder().build();

    String tokenFor(String user) throws Exception {
        String body = mvc.post().uri("/token").with(httpBasic(user, user + "-pass")).exchange()
                .getResponse().getContentAsString();
        return json.readTree(body).get("access_token").asString();
    }

    static String decodePart(String jwt, int i) {
        return new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[i]), StandardCharsets.UTF_8);
    }

    /** Mint a token directly with the app's encoder, letting the test tweak claims. */
    String mint(JwtEncoder enc, Consumer<JwtClaimsSet.Builder> tweak) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().issuer("https://auth.demo.test")
                .audience(List.of("notes-api")).subject("alice").issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(5))).claim("scope", "messages.read");
        tweak.accept(claims);
        return enc.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build(),
                claims.build())).getTokenValue();
    }

    MockHttpServletResponse get(String url, String token) {
        var req = mvc.get().uri(url);
        if (token != null) req = req.header("Authorization", "Bearer " + token);
        return req.exchange().getResponse();
    }

    static void show(String label, MockHttpServletResponse r) throws Exception {
        String www = r.getHeader("WWW-Authenticate");
        System.out.printf("%-34s %d %s%n", label, r.getStatus(), www != null ? "WWW-Authenticate: " + www : r.getContentAsString());
    }

    @Test
    void issueAndInspectAToken() throws Exception {
        String jwt = tokenFor("bob");
        System.out.println("token (3 base64url parts) : " + jwt.substring(0, 30) + "..." + " length=" + jwt.length());
        System.out.println("header  : " + decodePart(jwt, 0));
        System.out.println("payload : " + decodePart(jwt, 1)
                .replaceAll("\"(iat|exp)\":\\d+", "\"$1\":<epoch>").replaceAll("\"jti\":\"[^\"]+\"", "\"jti\":\"<uuid>\""));
        assertThat(decodePart(jwt, 0)).contains("\"alg\":\"RS256\"").contains("\"kid\":\"demo-key-1\"");
    }

    @Test
    void statusCodesWithRealTokens() throws Exception {
        String alice = tokenFor("alice");   // scope=messages.read
        String bob = tokenFor("bob");       // scope=messages.read messages.write, roles=[ADMIN]

        MockHttpServletResponse none = get("/api/messages", null);
        MockHttpServletResponse read = get("/api/messages", alice);
        MockHttpServletResponse write = mvc.post().uri("/api/messages").contentType(MediaType.TEXT_PLAIN)
                .content("hi").header("Authorization", "Bearer " + alice).exchange().getResponse();
        MockHttpServletResponse bobWrite = mvc.post().uri("/api/messages").contentType(MediaType.TEXT_PLAIN)
                .content("hi").header("Authorization", "Bearer " + bob).exchange().getResponse();
        MockHttpServletResponse admin = get("/api/admin/audit", bob);
        MockHttpServletResponse garbage = get("/api/messages", "not-a-jwt");

        show("no token", none);
        show("alice GET (messages.read)", read);
        show("alice POST (needs messages.write)", write);
        show("bob POST", bobWrite);
        show("bob GET admin (roles=[ADMIN])", admin);
        show("garbage token", garbage);

        assertThat(none.getStatus()).isEqualTo(401);
        assertThat(none.getHeader("WWW-Authenticate")).startsWith("Bearer");
        assertThat(read.getStatus()).isEqualTo(200);
        assertThat(write.getStatus()).isEqualTo(403);
        assertThat(write.getHeader("WWW-Authenticate")).contains("insufficient_scope");
        assertThat(bobWrite.getStatus()).isEqualTo(201);
        assertThat(admin.getStatus()).isEqualTo(200);
        assertThat(garbage.getStatus()).isEqualTo(401);
    }

    @Test
    void validationRules() throws Exception {
        var otherKeys = KeyPairGenerator.getInstance("RSA");
        otherKeys.initialize(2048);
        var kp = otherKeys.generateKeyPair();
        JwtEncoder attacker = NimbusJwtEncoder.withKeyPair((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()).build();
        Instant now = Instant.now();

        String good = mint(encoder, c -> {});
        String expiredLongAgo = mint(encoder, c -> c.issuedAt(now.minusSeconds(3600)).expiresAt(now.minusSeconds(600)));
        String expiredJustNow = mint(encoder, c -> c.issuedAt(now.minusSeconds(600)).expiresAt(now.minusSeconds(30)));
        String wrongIssuer = mint(encoder, c -> c.issuer("https://evil.test"));
        String wrongAudience = mint(encoder, c -> c.audience(List.of("billing-api")));
        String otherKey = mint(attacker, c -> {});
        // take a valid token and swap in a payload that claims more scopes
        String[] parts = good.split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                decodePart(good, 1).replace("messages.read", "messages.read messages.write").getBytes(StandardCharsets.UTF_8));
        String tampered = parts[0] + "." + forgedPayload + "." + parts[2];

        show("valid", get("/api/messages", good));
        show("expired 10 min ago", get("/api/messages", expiredLongAgo));
        show("expired 30 s ago (clock skew 60 s)", get("/api/messages", expiredJustNow));
        show("wrong issuer", get("/api/messages", wrongIssuer));
        show("wrong audience", get("/api/messages", wrongAudience));
        show("signed with another key", get("/api/messages", otherKey));
        show("payload edited after signing", get("/api/messages", tampered));

        assertThat(get("/api/messages", good).getStatus()).isEqualTo(200);
        assertThat(get("/api/messages", expiredLongAgo).getStatus()).isEqualTo(401);
        assertThat(get("/api/messages", expiredJustNow).getStatus()).isEqualTo(200);
        assertThat(get("/api/messages", wrongIssuer).getStatus()).isEqualTo(401);
        assertThat(get("/api/messages", wrongAudience).getStatus()).isEqualTo(401);
        assertThat(get("/api/messages", otherKey).getStatus()).isEqualTo(401);
        assertThat(get("/api/messages", tampered).getStatus()).isEqualTo(401);
    }

    @Test
    void protectedResourceMetadata() throws Exception {
        // RFC 9728: the resource server describes itself; the 401 header above points here
        var r = get("/.well-known/oauth-protected-resource", null);
        System.out.println("GET /.well-known/oauth-protected-resource -> " + r.getStatus() + " " + r.getContentAsString());
        assertThat(r.getStatus()).isEqualTo(200);
    }
}
