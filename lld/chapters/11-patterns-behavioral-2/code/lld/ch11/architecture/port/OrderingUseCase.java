package lld.ch11.architecture.port;

import java.util.Optional;

/** DRIVING (inbound) port: what the outside world may ask the application to do. */
public interface OrderingUseCase {
    OrderReceipt place(PlaceOrderCommand command);
    Optional<OrderReceipt> find(String orderId);
}
