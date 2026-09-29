package ch11.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Demo-only key management: a fresh RSA key pair per application start.
 * Real systems load the private key from a vault/KMS (or let an authorization server own it)
 * and resource servers fetch the public keys from the issuer's JWK Set URL.
 */
@Configuration
class JwtKeysConfig {

    @Bean
    KeyPair signingKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    // Signs tokens with the PRIVATE key (RS256 by default). Framework 7 / Security 7 builder API.
    @Bean
    JwtEncoder jwtEncoder(KeyPair keys) {
        return NimbusJwtEncoder.withKeyPair((RSAPublicKey) keys.getPublic(), (RSAPrivateKey) keys.getPrivate())
                .jwkPostProcessor(jwk -> jwk.keyID("demo-key-1"))
                .build();
    }

    // Verifies tokens with the PUBLIC key, then runs the validators (exp/nbf, iss, typ, aud)
    @Bean
    JwtDecoder jwtDecoder(KeyPair keys, @Value("${demo.jwt.issuer}") String issuer,
                          @Value("${demo.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic()).build();
        OAuth2TokenValidator<Jwt> audienceCheck =
                new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(audience));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer), audienceCheck));
        return decoder;
    }
}
