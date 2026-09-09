package com.github.vadymtrach.rmasystemshowcase.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserChangePasswordRequest(@NotBlank @Size(min = 6, max = 100) String currentPassword,
                                        @NotBlank @Size(min = 6, max = 100) String newPassword) {

}
