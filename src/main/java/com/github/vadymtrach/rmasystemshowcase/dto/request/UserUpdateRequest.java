package com.github.vadymtrach.rmasystemshowcase.dto.request;


import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(@NotBlank @Email
                                String email,
                                @NotBlank @Size(max = 100)
                                String fullName,
                                @NotNull
                                Role role) {
}
