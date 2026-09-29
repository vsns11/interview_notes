package com.example.scopes;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController                                  // a singleton...
public class CartController {
    private final Cart cart;                     // ...holding proxies that find the right
    private final RequestInfo request;           //    session/request instance on every call

    CartController(Cart cart, RequestInfo request) {
        this.cart = cart;
        this.request = request;
    }

    @PostMapping("/cart/{item}")
    String add(@PathVariable String item) {
        cart.add(item);
        List<String> items = cart.items();
        return "request#" + request.id() + " cart=" + items + " proxy=" + cart.getClass().getSimpleName().contains("CGLIB");
    }
}
