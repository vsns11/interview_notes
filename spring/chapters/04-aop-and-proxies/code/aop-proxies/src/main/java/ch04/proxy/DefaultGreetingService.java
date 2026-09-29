package ch04.proxy;

import org.springframework.stereotype.Service;

/** Implements an interface. With Boot's defaults it still gets a CGLIB (subclass) proxy. */
@Service
public class DefaultGreetingService implements GreetingService {
    @Override
    public String greet(String name) { return "Hello, " + name; }
}
