package ch12.caching;

import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;

/** Prints, per test class, how long getting the context took and the shared cache's statistics. */
public class CacheReportListener implements TestExecutionListener, Ordered {

    private long start;
    private Class<?> reported;

    /** The JVM-wide context cache is reachable through this protected accessor. */
    static final class Stats extends DefaultCacheAwareContextLoaderDelegate {
        static ContextCache cache() { return new Stats().getContextCache(); }
    }

    @Override public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }

    @Override public void beforeTestClass(TestContext tc) { start = System.nanoTime(); }

    @Override
    public void prepareTestInstance(TestContext tc) {
        if (tc.getTestClass() == reported) return;
        reported = tc.getTestClass();
        tc.getApplicationContext();                         // loads it, or takes it from the cache
        long ms = (System.nanoTime() - start) / 1_000_000;
        ContextCache c = Stats.cache();
        System.out.printf("%-26s context in %4d ms | cache size=%d hits=%d misses=%d | contexts built=%d%n",
                tc.getTestClass().getSimpleName(), ms, c.size(), c.getHitCount(), c.getMissCount(),
                CachingApplication.SlowStartup.CREATED.get());
    }
}
