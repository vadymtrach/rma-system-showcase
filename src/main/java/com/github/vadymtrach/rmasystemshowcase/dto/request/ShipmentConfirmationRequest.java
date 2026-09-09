package com.github.vadymtrach.rmasystemshowcase.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ShipmentConfirmationRequest(@NotNull LocalDate sentToClient) {
}
