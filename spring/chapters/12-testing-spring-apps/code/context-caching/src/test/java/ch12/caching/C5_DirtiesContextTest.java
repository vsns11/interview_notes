package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext   // default: close and evict the context AFTER this class
class C5_DirtiesContextTest extends BaseIntegrationTest {   // HIT, but then the context is thrown away
    @Autowired ConfigurableApplicationContext ctx;

    @Test void pretendToBreakSomething() { assertThat(ctx.isActive()).isTrue(); }
}
