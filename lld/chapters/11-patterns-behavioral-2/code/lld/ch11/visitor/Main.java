package lld.ch11.visitor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Classic Visitor (double dispatch) over a composite plan ===");
        Resource plan = new ResourceGroup("prod", List.of(
            new Server("web-1", 4, true),
            new Server("worker-1", 2, false),
            new Database("orders-db", 200, false),
            new LoadBalancer("edge-lb", 4),
            new ResourceGroup("staging", List.of())));

        MonthlyCost cost = new MonthlyCost();
        plan.accept(cost);
        System.out.println("  monthly cost: " + cost.total());

        SecurityAudit audit = new SecurityAudit();
        plan.accept(audit);
        audit.findings().forEach(f -> System.out.println("  finding: " + f));

        System.out.println("\n=== 2. Java 21: sealed interface + pattern matching switch ===");
        List<ModernPlan.Infra> infra = List.of(
            new ModernPlan.Vm("web-1", 4, true), new ModernPlan.Vm("worker-1", 2, false),
            new ModernPlan.Sql("orders-db", 200, false), new ModernPlan.Lb("edge-lb", 4));
        BigDecimal total = infra.stream().map(ModernPlan::monthlyCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("  monthly cost: " + total);
        infra.stream().map(ModernPlan::audit).flatMap(Optional::stream)
             .forEach(f -> System.out.println("  finding: " + f));
    }
}
