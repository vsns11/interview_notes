package ch04.proxy;

import ch04.aop.CallLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/** Advises every method of beans in ch04.proxy, so Spring must proxy them. */
@Aspect
@Component
public class TracingAspect {
    private final CallLog log;

    public TracingAspect(CallLog log) { this.log = log; }

    @Before("within(ch04.proxy..*) && !within(ch04.proxy.TracingAspect)")
    public void trace(JoinPoint jp) {
        log.add("trace " + jp.getSignature().toShortString());
    }
}
