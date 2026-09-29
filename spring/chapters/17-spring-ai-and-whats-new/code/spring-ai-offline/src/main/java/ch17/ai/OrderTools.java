package ch17.ai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** Methods the model may ask the application to run. */
public class OrderTools {

    public record OrderStatus(String orderId, String status, String eta) {}

    public final List<String> calls = new ArrayList<>();

    @Tool(name = "orderStatus", description = "Look up the delivery status of an order by its id, e.g. A-1001")
    public OrderStatus orderStatus(@ToolParam(description = "the order id") String orderId) {
        calls.add(orderId);
        return new OrderStatus(orderId, "SHIPPED", "2026-10-02");
    }
}
