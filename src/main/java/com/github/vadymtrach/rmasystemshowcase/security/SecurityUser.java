package com.github.vadymtrach.rmasystemshowcase.security;

import com.github.vadymtrach.rmasystemshowcase.entity.User;
import com.github.vadymtrach.rmasystemshowcase.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record SecurityUser(Long id,
                           String email,
                           String password,
                           String fullName,
                           Role role,
                           boolean active
                           ) implements UserDetails {


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isEnabled() {
        return this.active;
    }
}
