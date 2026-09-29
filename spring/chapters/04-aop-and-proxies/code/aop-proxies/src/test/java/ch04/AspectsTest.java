package ch04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch04.aop.CallLog;
import ch04.aop.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AspectsTest {

    @Autowired OrderService orders;   // this is the PROXY, not the raw OrderService
    @Autowired CallLog log;

    @BeforeEach
    void reset() { log.clear(); }

    @Test
    void adviceRunsAroundASuccessfulCall() {
        String result = orders.placeOrder("book", 2);
        System.out.println("result = " + result);
        log.lines().forEach(l -> System.out.println("  " + l));
        assertThat(log.lines()).containsExactly(
                "before placeOrder args=[book, 2]",
                "afterReturning placeOrder -> order-book-x2",
                "after placeOrder");
    }

    @Test
    void adviceRunsWhenTheMethodThrows() {
        assertThatThrownBy(() -> orders.cancel("unknown-7"))
                .isInstanceOf(IllegalArgumentException.class);   // the aspect does not swallow it
        log.lines().forEach(l -> System.out.println("  " + l));
        assertThat(log.lines()).containsExactly(
                "before cancel args=[unknown-7]",
                "afterThrowing cancel -> no such order: unknown-7",
                "after cancel");
    }

    @Test
    void twoAspectsNestByOrder() throws Exception {
        orders.monthlyReport();
        log.lines().forEach(l -> System.out.println("  " + l));
        assertThat(log.lines()).containsExactly(
                "before monthlyReport args=[]",        // @Order(1) LoggingAspect: outer
                "timer start [monthly-report]",        // @Order(2) TimingAspect: inner
                "timer stop [monthly-report] took>=20ms? true",
                "afterReturning monthlyReport -> report ready",
                "after monthlyReport");
    }
}
