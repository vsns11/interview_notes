package ch14.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;

/** Does a slow fixed-rate job overlap with itself? It depends on the scheduler. */
class OverlapTest {

    @Nested
    @SpringBootTest(properties = {"app.slow-job.enabled=true", "app.jobs.enabled=true"})
    class PlatformThreads {
        @Autowired SlowJob job;
        @Autowired TaskScheduler scheduler;

        @Test
        void runsAreQueuedNeverOverlapping() throws InterruptedException {
            Thread.sleep(1200);
            System.out.println(scheduler.getClass().getSimpleName() + ": runs=" + job.runs.get()
                    + " maxRunningAtOnce=" + job.maxRunningAtOnce.get() + " (" + job.thread + ")");
            assertThat(job.maxRunningAtOnce.get()).isEqualTo(1);
        }
    }

    @Nested
    @SpringBootTest(properties = {"app.slow-job.enabled=true", "spring.threads.virtual.enabled=true"})
    class VirtualThreads {
        @Autowired SlowJob job;
        @Autowired TaskScheduler scheduler;

        @Test
        void eachRunGetsItsOwnThreadSoRunsOverlap() throws InterruptedException {
            Thread.sleep(1200);
            System.out.println(scheduler.getClass().getSimpleName() + ": runs=" + job.runs.get()
                    + " maxRunningAtOnce=" + job.maxRunningAtOnce.get() + " (" + job.thread + ")");
            assertThat(job.maxRunningAtOnce.get()).isGreaterThan(1);
        }
    }
}
