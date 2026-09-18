package com.github.vadymtrach.rmasystemshowcase.service;

import com.github.vadymtrach.rmasystemshowcase.dto.request.UserChangePasswordRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserCreateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.request.UserUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.dto.response.UserResponse;
import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.exception.BusinessLogicException;
import com.github.vadymtrach.rmasystemshowcase.exception.ConflictException;
import com.github.vadymtrach.rmasystemshowcase.exception.ResourceNotFoundException;
import com.github.vadymtrach.rmasystemshowcase.mapper.UserMapper;
import com.github.vadymtrach.rmasystemshowcase.repository.UserRepository;
import com.github.vadymtrach.rmasystemshowcase.security.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserSessionService userSessionService;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(User.normalizeEmail(request.email()))) {
            throw new ConflictException("User with email " + request.email() + " already exists");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponse getUser(Long id) {
        return userMapper.toResponseDTO(findUserById(id));
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User existing = findUserById(id);
        if (userRepository.existsByEmailAndIdNot(User.normalizeEmail(request.email()), id)) {
            throw new ConflictException("User with email " + request.email() + " already exists");
        }
        if (request.role() != Role.ADMIN) {
            ensureNotLastActiveAdmin(existing);
        }
        boolean identityChanged = existing.getRole() != request.role()
                || !existing.getEmail().equals(User.normalizeEmail(request.email()));
        userMapper.updateEntityFromRequest(request, existing);
        if (identityChanged) {
            userSessionService.expireAllSessions(id);
        }
        return userMapper.toResponseDTO(existing);
    }

    @Transactional
    public void updateStatus(Long id, boolean status) {
        User existing = findUserById(id);
        if (!status) {
            ensureNotLastActiveAdmin(existing);
        }
        existing.setActive(status);
        if (!status) {
            userSessionService.expireAllSessions(id);
        }
    }

    @Transactional
    public void changePassword(Long id, UserChangePasswordRequest request, String currentSessionId) {
        User existing = findUserById(id);

        if (!passwordEncoder.matches(request.currentPassword(), existing.getPassword())) {
            throw new BusinessLogicException("Current password mismatch");
        }
        if (passwordEncoder.matches(request.newPassword(), existing.getPassword())) {
            throw new BusinessLogicException("New password must be different");
        }

        existing.setPassword(passwordEncoder.encode(request.newPassword()));
        userSessionService.expireOtherSessions(id, currentSessionId);
    }

    private void ensureNotLastActiveAdmin(User user) {
        if (user.getRole() == Role.ADMIN && user.isActive()
                && userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new BusinessLogicException("Cannot deactivate or demote the last active admin");
        }
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}