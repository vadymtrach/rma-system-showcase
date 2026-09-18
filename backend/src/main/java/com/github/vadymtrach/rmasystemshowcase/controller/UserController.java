package com.github.vadymtrach.rmasystemshowcase.controller;

import com.github.vadymtrach.rmasystemshowcase.dto.request.UserChangePasswordRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserCreateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserStatusUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.response.UserResponse;
import com.github.vadymtrach.rmasystemshowcase.security.SecurityUser;
import com.github.vadymtrach.rmasystemshowcase.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<List<UserResponse>> getUsers(){
        return ResponseEntity.ok(userService.getUsers());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal SecurityUser securityUser){
        return ResponseEntity.ok(userService.getUser(securityUser.id()));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody UserChangePasswordRequest request,
                                                 @AuthenticationPrincipal SecurityUser securityUser,
                                                 HttpSession session){
        userService.changePassword(securityUser.id(), request, session.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id)  {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserUpdateRequest request){
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                               @Valid @RequestBody UserStatusUpdateRequest request){
        userService.updateStatus(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
