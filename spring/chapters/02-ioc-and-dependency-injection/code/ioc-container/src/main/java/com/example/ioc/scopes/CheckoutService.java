package com.example.ioc.scopes;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service                                            // singleton that depends on a prototype
public class CheckoutService {
    private final ShoppingCart injectedOnce;                // injected ONE time, at creation
    private final ObjectProvider<ShoppingCart> carts;        // asks the container each time

    public CheckoutService(ShoppingCart injectedOnce, ObjectProvider<ShoppingCart> carts) {
        this.injectedOnce = injectedOnce;
        this.carts = carts;
    }

    public int cartIdFromField() { return injectedOnce.id; }
    public int cartIdFromProvider() { return carts.getObject().id; }
}
