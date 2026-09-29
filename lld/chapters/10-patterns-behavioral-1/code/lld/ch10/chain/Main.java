package lld.ch10.chain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. API request pipeline (classic linked handlers) ===");
        // Order is a design decision: auth BEFORE rate limiting so the limiter can key on the user,
        // and so anonymous traffic cannot burn a real user's quota.
        Handler pipeline = new AuthHandler();
        pipeline.setNext(new RateLimitHandler(2))
                .setNext(new ValidationHandler())
                .setNext(new CreateOrderHandler());

        List<Request> requests = List.of(
            new Request("alice", "t-123", "2 x widget"),
            new Request("bob", "", "1 x gadget"),
            new Request("alice", "t-123", " "),
            new Request("alice", "t-123", "1 x gadget"));
        for (Request r : requests) {
            System.out.println("request from " + r.user() + ":");
            System.out.println("  => handled = " + pipeline.handle(r));
        }

        System.out.println("\n=== 2. Expense approval chain (first capable handler wins) ===");
        Approver chain = new Approver("TeamLead", new BigDecimal("1000"));
        chain.then(new Approver("Manager", new BigDecimal("10000")))
             .then(new Approver("Director", new BigDecimal("50000")));
        for (Expense e : List.of(new Expense("EXP-1", new BigDecimal("300")),
                                 new Expense("EXP-2", new BigDecimal("7500")),
                                 new Expense("EXP-3", new BigDecimal("120000")))) {
            System.out.println("  " + chain.approve(e));
        }

        System.out.println("\n=== 3. Servlet-style filters (next is an explicit argument) ===");
        Filter timing = (req, res, next) -> {
            System.out.println("  [timing] start " + req.path());
            next.doFilter(req, res);                       // filters can act before AND after
            System.out.println("  [timing] end   " + req.path() + " -> " + res);
        };
        Filter auth = (req, res, next) -> {
            if (!req.headers().containsKey("Authorization")) {
                res.append("401 Unauthorized");            // does not call next: chain stops
                return;
            }
            next.doFilter(req, res);
        };
        Endpoint ordersEndpoint = (req, res) -> res.append("200 OK orders list");

        for (HttpRequest req : List.of(
                new HttpRequest("/orders", Map.of("Authorization", "Bearer abc")),
                new HttpRequest("/orders", Map.of()))) {
            new FilterChain(List.of(timing, auth), ordersEndpoint).doFilter(req, new StringBuilder());
        }

        System.out.println("\n=== 4. Logging levels (variant: every handler may act) ===");
        LogHandler logger = new ConsoleLog();
        logger.setNext(new FileLog()).setNext(new PagerLog());
        System.out.println("  log DEBUG:");
        logger.log(Level.DEBUG, "cache miss for key=42");
        System.out.println("  log ERROR:");
        logger.log(Level.ERROR, "payment service down");
    }
}
