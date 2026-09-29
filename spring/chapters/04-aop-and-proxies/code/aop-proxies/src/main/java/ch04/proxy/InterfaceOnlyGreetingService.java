package ch04.proxy;

import org.springframework.context.annotation.ProxyType;
import org.springframework.context.annotation.Proxyable;
import org.springframework.stereotype.Service;

/** Framework 7: @Proxyable overrides the global default for this one bean. */
@Service
@Proxyable(ProxyType.INTERFACES)       // ask for a JDK dynamic proxy
public class InterfaceOnlyGreetingService implements GreetingService {
    @Override
    public String greet(String name) { return "Hi, " + name; }
}
