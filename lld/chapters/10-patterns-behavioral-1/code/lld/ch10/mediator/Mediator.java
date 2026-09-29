package lld.ch10.mediator;

/** Components report "something happened" here. They depend on this interface only. */
public interface Mediator {
    void notify(Component sender, String event);
}
