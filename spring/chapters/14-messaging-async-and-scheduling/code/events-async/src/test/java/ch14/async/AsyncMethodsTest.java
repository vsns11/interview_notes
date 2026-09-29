package ch14.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;

@SpringBootTest
class AsyncMethodsTest {

    @Autowired ReportService reports;
    @Autowired Trace trace;
    @Autowired AsyncTaskExecutor applicationTaskExecutor;

    @BeforeEach
    void setUp() { trace.clear(); MDC.put("requestId", "r-42"); }

    @AfterEach
    void tearDown() { MDC.clear(); }

    @Test
    void executorsAndThreadNames() {
        System.out.println("auto-configured executor: " + applicationTaskExecutor.getClass().getSimpleName());
        String a = reports.build("daily").join();
        String b = reports.buildBig("yearly").join();
        String c = reports.buildViaThis("self-call");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        assertThat(a).contains("on task-").endsWith("requestId=r-42");      // TaskDecorator copied the MDC
        assertThat(b).contains("on report-").endsWith("requestId=null");    // hand-made executor: no decorator
        assertThat(c).contains("on main");                                  // self-invocation bypassed the proxy
    }

    @Test
    void exceptionsInAsyncMethods() {
        CompletableFuture<String> future = reports.buildBroken("march");
        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
        String viaHandle = future.handle((ok, ex) -> "recovered from: " + ex.getMessage()).join();
        System.out.println("CompletableFuture: " + viaHandle);

        reports.fireAndForget("april");                   // returns at once, no exception for the caller
        await().atMost(Duration.ofSeconds(2)).until(() -> !trace.lines().isEmpty());
        System.out.println("void method:       " + trace.lines().getFirst());
        assertThat(trace.lines().getFirst()).startsWith("uncaught handler: fireAndForget threw cannot export april on task-");
    }
}
