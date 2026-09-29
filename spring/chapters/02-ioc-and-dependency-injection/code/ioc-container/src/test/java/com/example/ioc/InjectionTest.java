package com.example.ioc;

import com.example.ioc.injection.AlertService;
import com.example.ioc.injection.OtherStyles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InjectionTest {

    @Test
    void resolvesByTypePrimaryQualifierAndCollections() {
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.injection")) {
            AlertService s = ctx.getBean(AlertService.class);
            System.out.println("by type (@Primary): " + s.viaDefault("hi"));
            System.out.println("@Qualifier(email):  " + s.viaEmail("hi"));
            System.out.println("List (by @Order):   " + s.broadcast("hi"));
            System.out.println("Map keys:           " + s.byName().keySet());
            System.out.println("optional AuditTrail present? " + s.hasAudit());
            assertThat(s.viaDefault("hi")).isEqualTo("SMS: hi");
            assertThat(s.broadcast("hi")).containsExactly("SMS: hi", "EMAIL: hi", "PUSH: hi");
            assertThat(s.hasAudit()).isFalse();

            OtherStyles o = ctx.getBean(OtherStyles.class);
            assertThat(o).extracting("fieldInjected").isNotNull();
            assertThat(o).extracting("setterInjected").isNotNull();
        }
    }

    @Test
    void twoCandidatesAndNoTieBreaker_failsAtStartup() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext("com.example.ioc.ambiguous"))
                .isInstanceOf(UnsatisfiedDependencyException.class)
                .satisfies(e -> System.out.println("startup failed: " + e.getMessage()
                        .replaceAll(".*(No qualifying bean.*)", "$1")))
                .hasMessageContaining("expected single matching bean but found 2");
    }
}
