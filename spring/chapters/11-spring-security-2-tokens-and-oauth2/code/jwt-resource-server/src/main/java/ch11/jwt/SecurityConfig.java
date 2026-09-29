package ch11.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    /** POST /token: exchange a username+password (HTTP Basic) for a signed JWT. */
    @Bean
    @Order(1)
    SecurityFilterChain tokenEndpoint(HttpSecurity http) throws Exception {
        http.securityMatcher("/token")
            .authorizeHttpRequests(a -> a.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /** Everything else: a stateless resource server that only accepts Bearer JWTs. */
    @Bean
    @Order(2)
    SecurityFilterChain api(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.GET, "/api/messages").hasAuthority("SCOPE_messages.read")
                .requestMatchers(HttpMethod.POST, "/api/messages").hasAuthority("SCOPE_messages.write")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
            .csrf(c -> c.disable())                // no cookies -> no CSRF risk
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    ScopeAndRoleAuthorities scopeAndRoleAuthorities() {
        return new ScopeAndRoleAuthorities();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(ScopeAndRoleAuthorities authorities) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        converter.setPrincipalClaimName("sub");                 // Authentication#getName() = sub claim
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder enc) {
        return new InMemoryUserDetailsManager(
                User.withUsername("alice").password(enc.encode("alice-pass")).authorities("messages.read").build(),
                User.withUsername("bob").password(enc.encode("bob-pass"))
                        .authorities("messages.read", "messages.write", "ROLE_ADMIN").build());
    }
}
