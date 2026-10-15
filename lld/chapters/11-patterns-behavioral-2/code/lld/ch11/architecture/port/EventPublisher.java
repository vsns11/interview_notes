package lld.ch11.architecture.port;

import lld.ch11.architecture.domain.OrderEvent;

/** DRIVEN port: announce facts. The core does not know who listens. */
public interface EventPublisher {
    void publish(OrderEvent event);
}
