package com.github.vadymtrach.rmasystemshowcase.dto.response;

import com.github.vadymtrach.rmasystemshowcase.enums.Role;

public record UserResponse(Long id,
                           String email,
                           String fullName,
                           Role role,
                           boolean active) {
}
