package com.example.hello;

import org.springframework.stereotype.Service;

@Service                                   // a bean: Spring creates one instance and manages it
public class GreetingService {

    public Greeting greet(String name) {
        String message = "Hello, " + name + "!";
        return new Greeting(message, message.length());
    }
}
