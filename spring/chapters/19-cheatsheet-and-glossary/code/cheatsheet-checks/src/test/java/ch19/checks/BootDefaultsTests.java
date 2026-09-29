package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.time.Duration;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.LifecycleProperties;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Sheet 19.5: defaults Boot 4.1.1 applies when you set nothing. */
class BootDefaultsTests {

    @Test
    void hikariPoolDefaults() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class))
                .run(ctx -> {
                    HikariDataSource ds = ctx.getBean(HikariDataSource.class);
                    String url;
                    try (Connection c = ds.getConnection()) {            // starts (and validates) the pool
                        url = c.getMetaData().getURL();
                    }
                    System.out.println("DataSource = " + ds.getClass().getSimpleName() + ", maximumPoolSize = "
                            + ds.getMaximumPoolSize() + ", connectionTimeout = " + ds.getConnectionTimeout() + " ms");
                    System.out.println("no spring.datasource.url + H2 on the classpath -> " + url.substring(0, 12)
                            + "<random name>  (generate-unique-name=true)");
                    assertThat(ds.getMaximumPoolSize()).isEqualTo(10);
                    assertThat(ds.getConnectionTimeout()).isEqualTo(30_000);
                    assertThat(url).startsWith("jdbc:h2:mem:").doesNotContain("testdb");
                });
    }

    @Test
    void asyncExecutorDefaults() {
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class));
        runner.run(ctx -> {
            var executor = ctx.getBean("applicationTaskExecutor", ThreadPoolTaskExecutor.class);
            System.out.println("applicationTaskExecutor = ThreadPoolTaskExecutor, core = " + executor.getCorePoolSize()
                    + ", prefix = " + executor.getThreadNamePrefix()
                    + ", queue = " + (new TaskExecutionProperties().getPool().getQueueCapacity() == Integer.MAX_VALUE ? "unbounded" : "bounded"));
            assertThat(executor.getCorePoolSize()).isEqualTo(8);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("task-");
        });
        runner.withPropertyValues("spring.threads.virtual.enabled=true").run(ctx -> {
            Object executor = ctx.getBean("applicationTaskExecutor");
            System.out.println("with spring.threads.virtual.enabled=true -> " + executor.getClass().getSimpleName());
            assertThat(executor).isInstanceOf(SimpleAsyncTaskExecutor.class);
        });
    }

    @Test
    void schedulingAndShutdownDefaults() {
        int schedulerThreads = new TaskSchedulingProperties().getPool().getSize();
        Duration shutdownPhase = new LifecycleProperties().getTimeoutPerShutdownPhase();
        System.out.println("spring.task.scheduling.pool.size default = " + schedulerThreads
                + ", spring.lifecycle.timeout-per-shutdown-phase default = " + shutdownPhase);
        assertThat(schedulerThreads).isEqualTo(1);
        assertThat(shutdownPhase).isEqualTo(Duration.ofSeconds(30));
    }
}
