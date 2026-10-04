package com.github.vadymtrach.rmasystemshowcase.service;

import com.github.vadymtrach.rmasystemshowcase.dto.request.UserUpdateRequest;
import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import com.github.vadymtrach.rmasystemshowcase.exception.BusinessLogicException;
import com.github.vadymtrach.rmasystemshowcase.mapper.UserMapper;
import com.github.vadymtrach.rmasystemshowcase.repository.UserRepository;
import com.github.vadymtrach.rmasystemshowcase.security.UserSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserSessionService userSessionService;
    @InjectMocks
    private UserService userService;

    @Test
    void deactivatingLastActiveAdminIsRejected() {
        User user = new User();
        user.setActive(true);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN))
                .thenReturn(1L);

        assertThatThrownBy(() -> userService.updateStatus(1L, false))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("last active admin");
        assertThat(user.isActive()).isTrue();
        verifyNoInteractions(userSessionService);
    }

    @Test
    void deactivatingAdminIsAllowedIfOtherAdminExists() {
        User user = new User();
        user.setActive(true);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN))
                .thenReturn(2L);

        userService.updateStatus(1L, false);

        assertThat(user.isActive()).isFalse();
        verify(userSessionService).expireAllSessions(1L);
    }

    @Test
    void deactivatingNonAdminIsAllowedWithoutCountingAdmins() {
        User user = new User();
        user.setActive(true);
        user.setRole(Role.EMPLOYEE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.updateStatus(1L, false);

        assertThat(user.isActive()).isFalse();
        verify(userRepository, never())
                .countByRoleAndActiveTrue(Role.ADMIN);
        verify(userSessionService).expireAllSessions(1L);
    }

    @Test
    void demotingLastActiveAdminIsRejected() {
        User user = new User();
        user.setEmail("vadym@example.com");
        user.setFullName("Vadym");
        user.setActive(true);
        user.setRole(Role.ADMIN);

        UserUpdateRequest request = new UserUpdateRequest("vadym@example.com", "Vadym", Role.EMPLOYEE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.countByRoleAndActiveTrue(Role.ADMIN))
                .thenReturn(1L);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("last active admin");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verifyNoInteractions(userSessionService);
    }

    @Test
    void activatingUserDoesNotExpireSessions() {
        User user = new User();
        user.setActive(false);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.updateStatus(1L, true);

        assertThat(user.isActive()).isTrue();
        verifyNoInteractions(userSessionService);
        verify(userRepository, never())
                .countByRoleAndActiveTrue(Role.ADMIN);
    }
}
