package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
class FilterChainTest {

    @Autowired FilterChainProxy springSecurityFilterChain; // the bean DelegatingFilterProxy calls

    @Test
    void printTheFilters() {
        List<SecurityFilterChain> chains = springSecurityFilterChain.getFilterChains();
        System.out.println("security filter chains: " + chains.size());
        List<String> names = chains.getFirst().getFilters().stream().map(f -> f.getClass().getSimpleName()).toList();
        for (int i = 0; i < names.size(); i++) System.out.printf("%2d %s%n", i + 1, names.get(i));
        assertThat(names).containsSubsequence("SecurityContextHolderFilter", "CsrfFilter",
                "UsernamePasswordAuthenticationFilter", "BasicAuthenticationFilter",
                "ExceptionTranslationFilter", "AuthorizationFilter");
        assertThat(names.getLast()).isEqualTo("AuthorizationFilter");
    }
}
