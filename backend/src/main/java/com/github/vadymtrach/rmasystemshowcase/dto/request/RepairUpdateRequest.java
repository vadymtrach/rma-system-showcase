package com.github.vadymtrach.rmasystemshowcase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RepairUpdateRequest(@NotBlank String repairDescription,
                                  @NotNull LocalDate repairDate) {
}
