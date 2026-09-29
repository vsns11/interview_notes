package ch10.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class MethodSecurityTest {

    @Autowired OrderService orders;
    @Autowired MockMvcTester mvc;

    @Test
    void noAuthenticationAtAll() {
        assertThatThrownBy(() -> orders.refund(1))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        System.out.println("no SecurityContext, refund(1) -> AuthenticationCredentialsNotFoundException");
    }

    @Test
    @WithMockUser(username = "alice") // default role USER
    void ownerCanReadOwnOrders() {
        System.out.println("alice ordersOf(alice)        -> " + orders.ordersOf("alice"));
        assertThatThrownBy(() -> orders.ordersOf("carol")).isInstanceOf(AccessDeniedException.class);
        System.out.println("alice ordersOf(carol)        -> AccessDeniedException");
        System.out.println("alice all() after @PostFilter -> " + orders.all());
        assertThat(orders.all()).extracting(OrderService.Order::owner).containsOnly("alice");
        assertThatThrownBy(() -> orders.find(2)).isInstanceOf(AccessDeniedException.class);
        System.out.println("alice find(2) (carol's)      -> AccessDeniedException (@PostAuthorize)");
        assertThatThrownBy(() -> orders.refund(1)).isInstanceOf(AccessDeniedException.class);
        System.out.println("alice refund(1)              -> AccessDeniedException (needs order:refund)");
    }

    @Test
    @WithMockUser(username = "bob", roles = "ADMIN")
    void adminCanReadAnyonesOrders() {
        System.out.println("bob(ADMIN) ordersOf(carol)   -> " + orders.ordersOf("carol"));
        assertThat(orders.ordersOf("carol")).hasSize(1);
    }

    @Test
    @WithMockUser(username = "support", authorities = "order:refund")
    void authorityBasedMethod() {
        System.out.println("support refund(1)            -> " + orders.refund(1));
    }

    @Test
    void overHttpAccessDeniedBecomes403() {
        var r = mvc.get().uri("/orders/of/carol").with(user("alice").roles("USER")).exchange();
        System.out.println("GET /orders/of/carol as alice -> HTTP " + r.getResponse().getStatus());
        assertThat(r).hasStatus(403);
    }
}
