package ch15.obs;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // probes, info and the Prometheus scrape stay open (normally reachable only inside the cluster)
                .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                // every other actuator endpoint needs the OPS role
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("OPS")
                .anyRequest().permitAll())
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());   // demo API only; see chapter 10 for CSRF
        return http.build();
    }
}
