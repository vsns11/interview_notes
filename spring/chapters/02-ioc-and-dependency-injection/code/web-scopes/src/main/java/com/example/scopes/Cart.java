package com.example.scopes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope          // one instance per HTTP session (per logged-in browser)
public class Cart {
    private final List<String> items = new ArrayList<>();
    public synchronized void add(String item) { items.add(item); }
    public synchronized List<String> items() { return List.copyOf(items); }
}
