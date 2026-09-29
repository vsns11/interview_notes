package lld.ch11.visitor;

import java.math.BigDecimal;

/**
 * Operation 1: ALL pricing rules in one file. New instance per run (it accumulates).
 * Money is BigDecimal: exact decimal arithmetic, no binary rounding surprises.
 */
public final class MonthlyCost implements PlanVisitor {
    static final BigDecimal PER_VCPU = new BigDecimal("24.00");
    static final BigDecimal DB_BASE = new BigDecimal("40.00");
    static final BigDecimal PER_GB = new BigDecimal("0.25");
    static final BigDecimal LB_BASE = new BigDecimal("18.00");
    static final BigDecimal PER_RULE = new BigDecimal("1.50");

    private BigDecimal total = BigDecimal.ZERO;

    public BigDecimal total() { return total; }

    public void visit(Server s)        { total = total.add(PER_VCPU.multiply(BigDecimal.valueOf(s.vcpus()))); }
    public void visit(Database d)      { total = total.add(DB_BASE.add(PER_GB.multiply(BigDecimal.valueOf(d.storageGb())))); }
    public void visit(LoadBalancer lb) { total = total.add(LB_BASE.add(PER_RULE.multiply(BigDecimal.valueOf(lb.rules())))); }
    public void visit(ResourceGroup g) { /* groups are free */ }
}
