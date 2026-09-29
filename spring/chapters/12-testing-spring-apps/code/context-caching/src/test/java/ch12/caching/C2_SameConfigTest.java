package ch12.caching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

class C2_SameConfigTest extends BaseIntegrationTest {       // identical config -> cache HIT
    @Autowired ApplicationContext ctx;

    @Test void reusesContext() { assertThat(ctx.getBean(CachingApplication.GreetingService.class)).isNotNull(); }
}
