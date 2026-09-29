package com.example.ioc;

import com.example.ioc.postprocessor.Greeter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class PostProcessorTest {

    @Test
    void beanPostProcessorCanSwapTheBeanForAProxy() {
        Trace.clear();
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.postprocessor")) {
            Greeter g = ctx.getBean(Greeter.class);
            Trace.add("bean class = " + g.getClass().getName());
            g.greet("Ada");
        }
        Trace.print("post-processors");
        assertThat(Trace.lines().get(0)).contains("defined=true, created=false");
        assertThat(Trace.lines()).contains("-> friendlyGreeter.greet(Ada)", "<- Hi Ada");
    }
}
