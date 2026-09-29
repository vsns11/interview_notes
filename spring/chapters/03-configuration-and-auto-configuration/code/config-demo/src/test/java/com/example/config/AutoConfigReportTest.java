package com.example.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigReportTest {

    @Test
    void theConditionReportExplainsEachDecision() {
        try (var ctx = Apps.start()) {
            var report = ConditionEvaluationReport.get(ctx.getBeanFactory());
            var bySource = report.getConditionAndOutcomesBySource();
            for (String cls : List.of(
                    "org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration",
                    "org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration",
                    "org.springframework.boot.autoconfigure.aop.AopAutoConfiguration")) {
                var outcomes = bySource.get(cls);
                System.out.println(cls.substring(cls.lastIndexOf('.') + 1) + (outcomes.isFullMatch() ? "  MATCHED" : "  DID NOT MATCH"));
                outcomes.forEach(co -> System.out.println("   - " + co.getOutcome().getMessage()));
            }
            System.out.println("auto-configuration classes evaluated: " + bySource.keySet().stream()
                    .filter(k -> !k.contains("#") && !k.contains("$")).count());
            assertThat(bySource.get("org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration").isFullMatch()).isFalse();
        }
    }
}
