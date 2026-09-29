package lld.ch11.visitor;

import java.util.ArrayList;
import java.util.List;

/** Operation 2: added later without touching a single resource class. */
public final class SecurityAudit implements PlanVisitor {
    private final List<String> findings = new ArrayList<>();

    public List<String> findings() { return findings; }

    public void visit(Server s) {
        if (s.publicIp()) findings.add(s.name() + ": server has a public IP");
    }
    public void visit(Database d) {
        if (!d.encrypted()) findings.add(d.name() + ": storage not encrypted");
    }
    public void visit(LoadBalancer lb) {
        if (lb.rules() == 0) findings.add(lb.name() + ": load balancer has no rules");
    }
    public void visit(ResourceGroup g) {
        if (g.children().isEmpty()) findings.add(g.name() + ": empty group");
    }
}
