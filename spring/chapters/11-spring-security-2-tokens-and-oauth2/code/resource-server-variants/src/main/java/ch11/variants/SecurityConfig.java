package ch11.variants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    OpaqueTokenIntrospector introspector() {
        return new FakeIntrospector();
    }

    @Bean
    TenantKeys tenantKeys() throws Exception {
        return new TenantKeys();
    }

    /** /opaque/**: tokens are random strings; every request asks the issuer "is this token active?". */
    @Bean
    @Order(1)
    SecurityFilterChain opaqueChain(HttpSecurity http, OpaqueTokenIntrospector introspector) throws Exception {
        http.securityMatcher("/opaque/**")
            .authorizeHttpRequests(a -> a
                .requestMatchers("/opaque/notes/write").hasAuthority("SCOPE_notes.write")
                .anyRequest().hasAuthority("SCOPE_notes.read"))
            .oauth2ResourceServer(rs -> rs.opaqueToken(o -> o.introspector(introspector)))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /** /tenant/**: JWTs from several issuers; the "iss" claim picks which keys verify the token. */
    @Bean
    @Order(2)
    SecurityFilterChain multiTenantChain(HttpSecurity http, TenantKeys keys) throws Exception {
        Map<String, AuthenticationManager> managers = new ConcurrentHashMap<>();
        JwtIssuerAuthenticationManagerResolver resolver = new JwtIssuerAuthenticationManagerResolver(issuer -> {
            if (!keys.isTrusted(issuer)) return null;         // unknown issuer -> 401
            return managers.computeIfAbsent(issuer, iss -> {
                NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keys.publicKey(iss)).build();
                decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(iss));
                return new ProviderManager(new JwtAuthenticationProvider(decoder));
            });
        });
        http.securityMatcher("/tenant/**")
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .oauth2ResourceServer(rs -> rs.authenticationManagerResolver(resolver))
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
