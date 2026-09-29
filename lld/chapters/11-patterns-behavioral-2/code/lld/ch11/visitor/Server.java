package lld.ch11.visitor;

/** Records expose data through accessors, so visitors need no public fields. */
public record Server(String name, int vcpus, boolean publicIp) implements Resource {
    public void accept(PlanVisitor v) { v.visit(this); }   // 2nd dispatch: overload picked by static type Server
}
