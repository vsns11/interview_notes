package ch14.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jobs.enabled=false")
class JobsDisabledTest {

    @Autowired Jobs jobs;

    @Test
    void noBackgroundRunsWhenSchedulingIsOff() throws InterruptedException {
        Thread.sleep(700);
        System.out.println("app.jobs.enabled=false -> runs after 700 ms: " + jobs.rateRuns.size());
        assertThat(jobs.rateRuns).isEmpty();
    }
}
