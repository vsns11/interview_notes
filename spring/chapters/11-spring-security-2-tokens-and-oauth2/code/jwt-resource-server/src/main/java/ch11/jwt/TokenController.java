package ch11.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** A tiny self-issued token endpoint (fine for demos/internal tools; real apps use an authorization server). */
@RestController
class TokenController {

    record TokenResponse(String access_token, String token_type, long expires_in) {}

    private final JwtEncoder encoder;
    private final String issuer;
    private final String audience;

    TokenController(JwtEncoder encoder, @Value("${demo.jwt.issuer}") String issuer,
                    @Value("${demo.jwt.audience}") String audience) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.audience = audience;
    }

    @PostMapping("/token")
    TokenResponse token(Authentication auth) {
        Instant now = Instant.now();
        Duration ttl = Duration.ofMinutes(15);
        List<String> authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("FACTOR_")).toList();
        String scope = String.join(" ", authorities.stream().filter(a -> !a.startsWith("ROLE_")).toList());
        List<String> roles = authorities.stream().filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .subject(auth.getName())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .id(UUID.randomUUID().toString())
                .claim("scope", scope)
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").build();
        String jwt = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(jwt, "Bearer", ttl.toSeconds());
    }
}
