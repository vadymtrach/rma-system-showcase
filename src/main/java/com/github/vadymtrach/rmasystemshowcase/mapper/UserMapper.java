package com.github.vadymtrach.rmasystemshowcase.mapper;

import com.github.vadymtrach.rmasystemshowcase.dto.request.UserCreateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.response.UserResponse;
import com.github.vadymtrach.rmasystemshowcase.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateRequest request){
        User user = new User();

        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFullName(request.fullName());
        user.setRole(request.role());

        return user;
    }

    public UserResponse toResponseDTO(User user){
        return new UserResponse(user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive());
    }

    public User updateEntityFromRequest(UserUpdateRequest updateRequest, User existing){
        existing.setEmail(updateRequest.email());
        existing.setFullName(updateRequest.fullName());
        existing.setRole(updateRequest.role());

        return existing;
    }
}
