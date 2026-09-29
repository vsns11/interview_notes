package ch04.features;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ReportJob {

    @Async                                   // proxy hands the call to a TaskExecutor thread
    public CompletableFuture<String> build() {
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }

    public CompletableFuture<String> buildViaThis() {
        return this.build();                 // runs on the CALLER's thread
    }
}
