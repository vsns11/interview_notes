package ch12.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record NewProduct(
        @NotBlank @Pattern(regexp = "[A-Z]{3}-\\d{3}") String sku,
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal price) { }
