package com.github.vadymtrach.rmasystemshowcase.dto.request;

import com.github.vadymtrach.rmasystemshowcase.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComplaintUpdateRequest(@NotBlank @Size(max = 255) String rmaNumber,
                                     @NotNull ProductType productType,
                                     @NotBlank @Size(max = 5000) String description,
                                     @NotBlank @Size(max = 255) String deliveryAddress,
                                     @NotNull @PositiveOrZero BigDecimal insuranceAmount,
                                     @NotNull Long version) {
}
