package lld.ch10.mediator;

/** Base for every form widget: its ONLY reference is the mediator. */
public abstract class Component {
    protected Mediator mediator;

    void setMediator(Mediator mediator) { this.mediator = mediator; }
}
