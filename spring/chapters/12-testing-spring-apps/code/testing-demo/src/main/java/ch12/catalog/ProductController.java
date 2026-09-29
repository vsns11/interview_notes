package ch12.catalog;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final PriceService service;

    public ProductController(PriceService service) { this.service = service; }

    @GetMapping("/{sku}/price")
    public PriceView price(@PathVariable String sku,
                           @RequestParam(defaultValue = "EUR") String currency) {
        return service.priceOf(sku, currency);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody NewProduct cmd) {
        Product p = service.create(cmd);
        return ResponseEntity.created(URI.create("/api/products/" + p.getSku() + "/price")).build();
    }

    @ExceptionHandler
    ProblemDetail notFound(ProductNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
