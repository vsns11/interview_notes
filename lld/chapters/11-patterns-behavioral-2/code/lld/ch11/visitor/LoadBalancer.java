package lld.ch11.visitor;

public record LoadBalancer(String name, int rules) implements Resource {
    public void accept(PlanVisitor v) { v.visit(this); }
}
