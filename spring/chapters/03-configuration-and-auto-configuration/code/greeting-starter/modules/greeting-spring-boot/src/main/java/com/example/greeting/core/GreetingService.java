package com.example.greeting.core;

/** The "library" the starter wires up. It knows nothing about Spring. */
public class GreetingService {
    private final String template;
    public GreetingService(String template) { this.template = template; }
    public String greet(String name) { return template.formatted(name); }
}
