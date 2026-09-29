package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

class C6_AfterDirtiesTest extends BaseIntegrationTest {     // base config again, but evicted -> MISS
    @Autowired ConfigurableApplicationContext ctx;

    @Test void rebuilt() { assertThat(ctx.isActive()).isTrue(); }
}
