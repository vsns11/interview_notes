package ch19.checks.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * The Spring Security 7 snippets printed in section 19.8. They are compiled by every build of this
 * project (so the DSL calls exist in Security 7.1.1), but not started: there is no web server here.
 */
public final class SecuritySnippets {

    private SecuritySnippets() { }

    /** 19.8.1 Form login + HTTP Basic for a server-rendered app. */
    @Configuration(proxyBeanMethods = false)
    public static class FormLoginConfig {

        @Bean
        SecurityFilterChain web(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth                      // first matching rule wins
                    .requestMatchers("/", "/public/**", "/login").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")        // checks authority ROLE_ADMIN
                    .requestMatchers(HttpMethod.GET, "/reports/**").hasAuthority("report:read")
                    .anyRequest().authenticated())                        // or denyAll() to fail closed
                .formLogin(form -> form.loginPage("/login").permitAll()) // omit loginPage for the generated page
                .httpBasic(Customizer.withDefaults())
                .logout(Customizer.withDefaults());                        // POST /logout (+ CSRF token)
            return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {                                // {bcrypt} by default
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        UserDetailsService users(PasswordEncoder encoder) {               // replace with a JPA/JDBC-backed one
            return new InMemoryUserDetailsManager(
                    User.withUsername("alice").password(encoder.encode("alice-pass")).roles("USER").build(),
                    User.withUsername("bob").password(encoder.encode("bob-pass")).roles("ADMIN").build());
        }

        @Bean
        static RoleHierarchy roleHierarchy() {                             // admins also pass hasRole("USER")
            return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
        }
    }

    /** 19.8.2 A stateless JWT resource server next to the browser chain (two chains, @Order). */
    @Configuration(proxyBeanMethods = false)
    public static class JwtApiConfig {

        @Bean
        @Order(1)                                                          // specific chain first
        SecurityFilterChain api(HttpSecurity http) throws Exception {
            http.securityMatcher("/api/**")                              // which requests this chain owns
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_orders.read")
                    .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("SCOPE_orders.write")
                    .anyRequest().authenticated())
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))   // issuer-uri from properties
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());                           // no cookies on this chain
            return http.build();
        }
    }

    /** 19.8.3 Method security: rules that need arguments or return values. */
    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity                                                  // @PreAuthorize & co. (proxy-based)
    public static class MethodSecurityConfig {
        @Bean
        OrderService orderService() { return new OrderService(); }
    }

    public static class OrderService {
        public record Order(long id, String owner) { }

        @PreAuthorize("hasRole('ADMIN') or #owner == authentication.name")
        public List<Order> ordersOf(String owner) { return List.of(new Order(1, owner)); }

        @PostAuthorize("returnObject.owner() == authentication.name")
        public Order find(long id) { return new Order(id, "alice"); }

        @PreAuthorize("hasAuthority('order:refund')")
        public String refund(long id) { return "refunded " + id; }
    }

    /** 19.8.4 CORS before authentication, CSRF for a cookie-based single-page app. */
    @Configuration(proxyBeanMethods = false)
    public static class SpaConfig {

        @Bean
        SecurityFilterChain spa(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .cors(Customizer.withDefaults())         // uses the CorsConfigurationSource bean below
                .csrf(csrf -> csrf.spa())                 // Security 7: XSRF-TOKEN cookie + X-XSRF-TOKEN header
                .formLogin(Customizer.withDefaults());
            return http.build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration api = new CorsConfiguration();
            api.setAllowedOrigins(List.of("https://app.example.com"));   // exact origins, never "*" with credentials
            api.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            api.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
            api.setAllowCredentials(true);
            api.setMaxAge(3600L);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/api/**", api);
            return source;
        }
    }
}
