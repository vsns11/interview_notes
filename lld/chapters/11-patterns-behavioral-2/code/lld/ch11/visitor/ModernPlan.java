package lld.ch11.visitor;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * The Java 21 alternative: a SEALED hierarchy + pattern-matching switch.
 * No accept(), no visitor interface. The compiler still checks exhaustiveness:
 * add a new record to 'permits' and every switch without it stops compiling.
 */
public final class ModernPlan {
    private ModernPlan() {}

    public sealed interface Infra permits Vm, Sql, Lb {}
    public record Vm(String name, int vcpus, boolean publicIp) implements Infra {}
    public record Sql(String name, int storageGb, boolean encrypted) implements Infra {}
    public record Lb(String name, int rules) implements Infra {}

    /** Operation 1 as a plain function. No 'default' branch, on purpose. Same prices as MonthlyCost. */
    public static BigDecimal monthlyCost(Infra r) {
        return switch (r) {
            case Vm vm -> MonthlyCost.PER_VCPU.multiply(BigDecimal.valueOf(vm.vcpus()));
            case Sql db -> MonthlyCost.DB_BASE.add(MonthlyCost.PER_GB.multiply(BigDecimal.valueOf(db.storageGb())));
            case Lb lb -> MonthlyCost.LB_BASE.add(MonthlyCost.PER_RULE.multiply(BigDecimal.valueOf(lb.rules())));
        };
    }

    /** Operation 2 with record deconstruction patterns and guards. */
    public static Optional<String> audit(Infra r) {
        return switch (r) {
            case Vm(String n, int cpus, boolean pub) when pub -> Optional.of(n + ": server has a public IP");
            case Sql(String n, int gb, boolean enc) when !enc -> Optional.of(n + ": storage not encrypted");
            case Lb(String n, int rules) when rules == 0 -> Optional.of(n + ": load balancer has no rules");
            case Vm vm -> Optional.empty();     // no finding
            case Sql db -> Optional.empty();
            case Lb lb -> Optional.empty();
        };
    }
}
