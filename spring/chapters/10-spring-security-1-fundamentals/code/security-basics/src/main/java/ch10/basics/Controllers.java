package ch10.basics;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controllers {

    private final OrderService orders;

    Controllers(OrderService orders) { this.orders = orders; }

    @GetMapping("/public/hello") String hello() { return "hello, anyone"; }
    @GetMapping("/admin/stats")  String stats() { return "admin stats"; }
    @GetMapping("/user/profile") String profile() { return "profile page"; }
    @GetMapping("/reports/daily") String report() { return "daily report"; }

    // Spring MVC resolves the current Authentication (it comes from the SecurityContext)
    record Me(String name, String principalType, List<String> authorities) {}

    @GetMapping("/me")
    Me me(Authentication auth, @AuthenticationPrincipal UserDetails principal) {
        return new Me(auth.getName(), principal.getClass().getSimpleName(),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList());
    }

    @GetMapping("/orders/of/{owner}")
    List<OrderService.Order> ordersOf(@PathVariable String owner) { return orders.ordersOf(owner); }

    @GetMapping("/orders/{id}")
    OrderService.Order order(@PathVariable long id) { return orders.find(id); }

    @GetMapping("/orders")
    List<OrderService.Order> all() { return orders.all(); }
}
