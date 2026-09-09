package com.github.vadymtrach.rmasystemshowcase.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(@NotNull Boolean active) {
}
