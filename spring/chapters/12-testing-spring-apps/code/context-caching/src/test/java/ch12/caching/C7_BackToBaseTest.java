package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

class C7_BackToBaseTest extends BaseIntegrationTest {       // HIT on the rebuilt context
    @Autowired ConfigurableApplicationContext ctx;

    @Test void cached() { assertThat(ctx.isActive()).isTrue(); }
}
