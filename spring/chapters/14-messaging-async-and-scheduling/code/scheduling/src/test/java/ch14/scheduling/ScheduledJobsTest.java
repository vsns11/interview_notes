package ch14.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ScheduledJobsTest {

    @Autowired Jobs jobs;

    @Test
    void fixedRateVersusFixedDelayVersusCron() {
        await().atMost(Duration.ofSeconds(5))
                .until(() -> jobs.rateRuns.size() >= 6 && jobs.delayRuns.size() >= 4 && jobs.cronRuns.size() >= 2);

        List<Long> rateGaps = gaps(jobs.rateRuns);
        List<Long> delayGaps = gaps(jobs.delayRuns);
        System.out.println("fixedRate  = 300 ms, job takes 200 ms -> gaps between starts " + rateGaps
                + " on " + jobs.rateRuns.getFirst().thread());
        System.out.println("fixedDelay = 300 ms, job takes 200 ms -> gaps between starts " + delayGaps
                + " on " + jobs.delayRuns.getFirst().thread());
        System.out.println("cron */1 * * * * *                    -> " + jobs.cronRuns.subList(0, 2));

        assertThat(median(rateGaps)).isBetween(270L, 360L);    // about 300 = the period
        assertThat(median(delayGaps)).isBetween(470L, 600L);   // about 500 = 200 work + 300 delay
    }

    /** Rounded to 10 ms so the printed numbers are readable. */
    private static List<Long> gaps(List<Jobs.Run> runs) {
        List<Long> gaps = new ArrayList<>();
        for (int i = 1; i < Math.min(runs.size(), 5); i++) {
            gaps.add(Math.round((runs.get(i).startMillis() - runs.get(i - 1).startMillis()) / 10.0) * 10);
        }
        return gaps;
    }

    private static long median(List<Long> values) {
        List<Long> sorted = values.stream().sorted().toList();
        return sorted.get(sorted.size() / 2);
    }
}
