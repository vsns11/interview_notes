package com.example.ioc.postprocessor;

import org.springframework.stereotype.Component;

@Component
@LogCalls
public class FriendlyGreeter implements Greeter {
    public String greet(String name) { return "Hi " + name; }
}
