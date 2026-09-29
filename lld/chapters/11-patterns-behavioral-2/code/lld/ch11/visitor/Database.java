package lld.ch11.visitor;

public record Database(String name, int storageGb, boolean encrypted) implements Resource {
    public void accept(PlanVisitor v) { v.visit(this); }
}
