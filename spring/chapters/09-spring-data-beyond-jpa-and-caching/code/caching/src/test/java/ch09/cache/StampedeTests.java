package ch09.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StampedeTests {

    @Autowired ReportService reports;

    /** Start CALLERS threads that all ask for the same, not-yet-cached key at once. */
    static List<String> callTogether(Callable<String> call) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(ReportService.CALLERS)) {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < ReportService.CALLERS; i++) futures.add(pool.submit(call));
            List<String> results = new ArrayList<>();
            for (Future<String> f : futures) results.add(f.get());
            return results;
        }
    }

    @Test
    void withoutSyncEveryMissingThreadComputes() throws Exception {
        callTogether(() -> reports.plain("2026-09-28"));
        System.out.println(ReportService.CALLERS + " concurrent callers, sync=false -> computed "
                + reports.plainComputations() + " times");
        assertThat(reports.plainComputations()).isEqualTo(ReportService.CALLERS);
    }

    @Test
    void withSyncOnlyOneThreadComputes() throws Exception {
        List<String> results = callTogether(() -> reports.synced("2026-09-28"));
        System.out.println(ReportService.CALLERS + " concurrent callers, sync=true  -> computed "
                + reports.syncComputations() + " time, all got \"" + results.getFirst() + "\"");
        assertThat(reports.syncComputations()).isEqualTo(1);
        assertThat(results).containsOnly("report 2026-09-28");
    }
}
