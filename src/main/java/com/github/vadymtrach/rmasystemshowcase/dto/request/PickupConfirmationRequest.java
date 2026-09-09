package com.github.vadymtrach.rmasystemshowcase.dto.request;


import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PickupConfirmationRequest(@NotNull LocalDate pickupConfirmed) {
}
