package com.github.vadymtrach.rmasystemshowcase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RepairUpdateRequest(@NotBlank @Size(max = 5000) String repairDescription,
                                  @NotNull LocalDate repairDate) {
}
