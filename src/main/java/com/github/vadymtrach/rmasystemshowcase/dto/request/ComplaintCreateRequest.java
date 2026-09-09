package com.github.vadymtrach.rmasystemshowcase.dto.request;

import com.github.vadymtrach.rmasystemshowcase.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ComplaintCreateRequest(@NotBlank String rmaNumber,
                                     @NotNull ProductType productType,
                                     @NotBlank String description,
                                     @NotBlank String deliveryAddress,
                                     @NotNull @PositiveOrZero BigDecimal insuranceAmount) {

}
