package ch14.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Only overrides the exception handler. getAsyncExecutor() is not overridden, so @Async keeps
 * using Boot's auto-configured executor. Keep AsyncConfigurer in its own class with no @Bean methods:
 * Boot 4 wraps AsyncConfigurer beans.
 */
@Configuration(proxyBeanMethods = false)
public class AsyncErrorConfig implements AsyncConfigurer {

    private final Trace trace;

    public AsyncErrorConfig(Trace trace) { this.trace = trace; }

    /** Exceptions thrown by void @Async methods have no caller to go to - they land here. */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                trace.add("uncaught handler: " + method.getName() + " threw " + ex.getMessage() + " on " + Trace.thread());
    }
}
