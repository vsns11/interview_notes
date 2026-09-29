package lld.ch11.visitor;

/** ELEMENT interface: the only thing every resource must provide is accept. */
public interface Resource {
    void accept(PlanVisitor visitor);
}
