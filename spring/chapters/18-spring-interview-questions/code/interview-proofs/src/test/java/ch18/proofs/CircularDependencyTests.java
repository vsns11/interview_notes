package ch18.proofs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/** Q: "How does Spring handle circular dependencies?" */
class CircularDependencyTests {

    // --- constructor cycle: OrderService -> InvoiceService -> OrderService
    static class OrderService { OrderService(InvoiceService invoices) { } }
    static class InvoiceService { InvoiceService(OrderService orders) { } }
    @Import({OrderService.class, InvoiceService.class}) static class ConstructorCycle { }

    // --- the same cycle, broken with @Lazy (a lazy-resolution proxy is injected)
    static class LazyOrderService { LazyOrderService(@Lazy LazyInvoiceService invoices) { } }
    static class LazyInvoiceService { LazyInvoiceService(LazyOrderService orders) { } }
    @Import({LazyOrderService.class, LazyInvoiceService.class}) static class LazyCycle { }

    // --- a field-injection cycle
    static class FieldA { @Autowired FieldB b; }
    static class FieldB { @Autowired FieldA a; }
    @Import({FieldA.class, FieldB.class}) static class FieldCycle { }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }

    @Test
    void constructorCycleFailsAtStartup() {
        new ApplicationContextRunner().withUserConfiguration(ConstructorCycle.class).run(ctx -> {
            Throwable root = root(ctx.getStartupFailure());
            System.out.println("constructor cycle      -> " + root.getClass().getSimpleName());
            assertThat(root).isInstanceOf(BeanCurrentlyInCreationException.class);
        });
    }

    @Test
    void lazyBreaksTheCycle() {
        new ApplicationContextRunner().withUserConfiguration(LazyCycle.class).run(ctx -> {
            System.out.println("cycle with @Lazy       -> started: " + (ctx.getStartupFailure() == null));
            assertThat(ctx).hasNotFailed();
        });
    }

    @Test
    void fieldCycleIsRejectedByBootDefaultButAllowedWhenSwitchedOn() {
        // Boot's default (like SpringApplication since Boot 2.6): circular references NOT allowed
        new ApplicationContextRunner().withUserConfiguration(FieldCycle.class).run(ctx -> {
            System.out.println("field cycle, Boot default          -> "
                    + root(ctx.getStartupFailure()).getClass().getSimpleName());
            assertThat(ctx).hasFailed();
        });
        // spring.main.allow-circular-references=true equivalent: early references resolve the cycle
        new ApplicationContextRunner().withAllowCircularReferences(true)
                .withUserConfiguration(FieldCycle.class).run(ctx -> {
            System.out.println("field cycle, circular refs allowed -> started: "
                    + (ctx.getStartupFailure() == null));
            assertThat(ctx).hasNotFailed();
        });
    }
}
