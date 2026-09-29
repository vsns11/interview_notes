package ch04;

import static org.assertj.core.api.Assertions.assertThat;

import ch04.features.AccountService;
import ch04.features.PriceLookup;
import ch04.features.ReportJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SelfInvocationTest {

    @Autowired AccountService accounts;
    @Autowired PriceLookup prices;
    @Autowired ReportJob reports;

    @Test
    void transactionalSelfInvocation() {
        String external = accounts.transfer();
        String viaThis = accounts.transferViaThis();
        String viaSelf = accounts.transferViaSelfProxy();
        System.out.println("caller -> proxy -> transfer():        " + external);
        System.out.println("caller -> proxy -> this.transfer():   " + viaThis);
        System.out.println("caller -> proxy -> self.transfer():   " + viaSelf);
        assertThat(external).endsWith("true");
        assertThat(viaThis).endsWith("false");     // @Transactional silently ignored
        assertThat(viaSelf).endsWith("true");
    }

    @Test
    void cacheableSelfInvocation() {
        int before = prices.realCalls();
        prices.price("ABC");
        prices.price("ABC");                        // served from the cache
        int afterExternal = prices.realCalls() - before;
        prices.priceTwiceViaThis("XYZ");            // two internal calls, both miss the cache
        int afterInternal = prices.realCalls() - before - afterExternal;
        System.out.println("2 external calls -> real method ran " + afterExternal + " time(s)");
        System.out.println("2 internal calls -> real method ran " + afterInternal + " time(s)");
        assertThat(afterExternal).isEqualTo(1);
        assertThat(afterInternal).isEqualTo(2);
    }

    @Test
    void asyncSelfInvocation() throws Exception {
        String caller = Thread.currentThread().getName();
        String viaProxy = reports.build().get();
        String viaThis = reports.buildViaThis().get();
        System.out.println("caller thread:          " + caller);
        System.out.println("@Async through proxy:   " + viaProxy);
        System.out.println("@Async via this.build(): " + viaThis);
        assertThat(viaProxy).isNotEqualTo(caller);
        assertThat(viaThis).isEqualTo(caller);      // ran synchronously
    }
}
