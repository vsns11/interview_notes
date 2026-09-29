package ch15.obs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;
    private final RestClient.Builder restClientBuilder;   // Boot's builder: already instrumented
    private final Environment env;

    public OrderController(OrderService service, RestClient.Builder restClientBuilder, Environment env) {
        this.service = service;
        this.restClientBuilder = restClientBuilder;
        this.env = env;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> get(@PathVariable long id) {
        return ResponseEntity.of(service.find(id));
    }

    @PostMapping("/orders")
    public long place(@RequestBody String item) {
        return service.place(item);
    }

    /** Calls a "downstream service" over HTTP (here: this same app) to show trace propagation. */
    @GetMapping("/orders/{id}/price")
    public String price(@PathVariable long id) {
        log.info("asking pricing service for order {}", id);
        String base = "http://localhost:" + env.getProperty("local.server.port");
        return restClientBuilder.build().get()
                .uri(base + "/pricing/{id}", id)
                .retrieve().body(String.class);
    }

    @GetMapping("/pricing/{id}")
    public String pricing(@PathVariable long id) {
        log.info("pricing order {}", id);
        return "price for " + id + " = 49.99";
    }
}
